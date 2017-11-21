package com.gofobao.framework.listener.providers;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.JixinTenderRecordHelper;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.vo.VoFindAutoTenderList;
import com.gofobao.framework.tender.vo.VoSaveThirdTender;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by Zeke on 2017/6/2.
 */
@Component
@Slf4j
public class TenderProvider {

    @Autowired
    BorrowService borrowService;

    @Autowired
    AutoTenderService autoTenderService;

    @Autowired
    TenderBiz tenderBiz;

    @Autowired
    private JixinTenderRecordHelper jixinTenderRecordHelper;

    @Autowired
    private TenderThirdBiz tenderThirdBiz;

    /**
     * 自动投标
     *
     * @param msg
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoTender(Map<String, String> msg) throws Exception {
        //投标撤回集合
        Gson gson = new Gson();
        log.info(String.format("自动投标启动: %s", gson.toJson(msg)));
        Date nowDate = new Date();
        Long borrowId = NumberHelper.toLong(msg.get(MqConfig.MSG_BORROW_ID));
        Borrow borrow = borrowService.findByIdLock(borrowId);
        Preconditions.checkNotNull(borrow, "自动投标异常：id为" + borrowId + "借款不存在");
        VoFindAutoTenderList voFindAutoTenderList = new VoFindAutoTenderList();
        List<Map<String, Object>> autoTenderList = null;
        long moneyYes = borrow.getMoneyYes();

        int pageIndex = 0;
        int maxSize = 50;
        int autoTenderCount = 0; // 中标item
        Set<Long> tenderUserIds = new HashSet<>();
        Set<Long> autoTenderIds = new HashSet<>();
        //是否满标
        boolean isFull = false;
        // 借款金额（分）
        long borrowMoney = borrow.getMoney();
        //最大自动投标额
        Integer mostAuto = borrow.getMostAuto();
        try {
            do {
                voFindAutoTenderList.setStatus("1");
                voFindAutoTenderList.setNotUserId(borrow.getUserId());
                voFindAutoTenderList.setInRepayFashions(BorrowHelper.countRepayFashions(new Integer[]{borrow.getRepayFashion()}));
                voFindAutoTenderList.setPageIndex(pageIndex);
                voFindAutoTenderList.setPageSize(maxSize);
                voFindAutoTenderList.setBorrowId(borrowId);
                Integer apr = borrow.getApr();
                voFindAutoTenderList.setLtAprFirst(apr);
                voFindAutoTenderList.setGtAprLast(apr);
                // 查询自动投标队列
                autoTenderList = autoTenderService.findQualifiedAutoTenders(voFindAutoTenderList);
                if (CollectionUtils.isEmpty(autoTenderList)) {
                    log.info("自动投标MQ：第" + (pageIndex) + "页,没有匹配到自动投标规则！");
                    break;
                }

                pageIndex++;
                Iterator<Map<String, Object>> itAutoTender = autoTenderList.iterator();
                Map<String, Object> voFindAutoTender = null;
                long money = 0;
                long lowest = 0;
                long useMoney = 0;
                // 将合格的自动投标  放入消息队列
                while (itAutoTender.hasNext()) {
                    voFindAutoTender = itAutoTender.next();
                    // 判断是否满标或者 达到自动投标最大额度
                    if ((moneyYes >= borrowMoney) || (mostAuto > 0 && moneyYes >= mostAuto)) {
                        // 满标了
                        isFull = true;
                        break;
                    }
                    // 保证每个用户 和 每个自动投标规则只能使用一次
                    if (tenderUserIds.contains(NumberHelper.toLong(voFindAutoTender.get("userId")))
                            || autoTenderIds.contains(NumberHelper.toLong(voFindAutoTender.get("id")))) {
                        continue;
                    }
                    // 用户可用金额
                    useMoney = NumberHelper.toLong(voFindAutoTender.get("useMoney"));
                    // 0 余额  1.固定金额
                    money = String.valueOf(voFindAutoTender.get("mode")).equals("1") ? NumberHelper.toInt(voFindAutoTender.get("tenderMoney")) : useMoney;
                    money = Math.min(useMoney - NumberHelper.toInt(voFindAutoTender.get("saveMoney")), money);
                    // 最小投标金额
                    lowest = NumberHelper.toLong(voFindAutoTender.get("lowest"));
                    if ((money < lowest)) {
                        continue;
                    }

                    // 标的金额小于 最小投标金额
                    if (borrowMoney - moneyYes < lowest) {
                        continue;
                    }

                    VoCreateTenderReq voCreateBorrowTender = new VoCreateTenderReq();
                    // 标的
                    voCreateBorrowTender.setBorrowId(borrowId);
                    // 投标用户
                    voCreateBorrowTender.setUserId(NumberHelper.toLong(voFindAutoTender.get("userId")));
                    // 投标金额
                    voCreateBorrowTender.setTenderMoney(MoneyHelper.round(MoneyHelper.divide(money, 100d), 2));
                    voCreateBorrowTender.setAutoOrder(NumberHelper.toInt(voFindAutoTender.get("order")));
                    //自动标识
                    voCreateBorrowTender.setIsAutoTender(true);
                    //自动投标
                    voCreateBorrowTender.setRequestSource("0");
                    // 防止自动投标重复
                    if ((!tenderUserIds.contains(NumberHelper.toLong(voFindAutoTender.get("userId"))))
                            && (!autoTenderIds.contains(NumberHelper.toLong(voFindAutoTender.get("id"))))) {
                        ResponseEntity<VoBaseResp> response = tenderBiz.createTender(voCreateBorrowTender);
                        if (response.getBody().getState().getCode() == VoBaseResp.OK) {
                            moneyYes += money;
                            long autoTenderId = NumberHelper.toLong(voFindAutoTender.get("id"));
                            autoTenderIds.add(autoTenderId);
                            tenderUserIds.add(NumberHelper.toLong(voFindAutoTender.get("userId")));
                            AutoTender autoTender = autoTenderService.findById(autoTenderId);
                            autoTender.setId(autoTenderId);
                            autoTender.setAutoAt(new Date());
                            autoTender.setUpdatedAt(new Date());
                            autoTenderService.save(autoTender);
                            autoTenderCount++;
                        } else {
                            log.info(String.format("自动投标启动: 创建投标失败 %s,msg->%s", gson.toJson(voFindAutoTender), response.getBody().getState().getMsg()));
                            continue;
                        }
                    }
                }
            } while (autoTenderList.size() >= maxSize && !isFull);


            if (autoTenderCount >= 1) {
                autoTenderService.updateAutoTenderOrder();
                log.info("===========================AutoTenderListener===========================");
                log.info("自动投标成功! borrowId：" + borrowId);
                log.info("========================================================================");

            } else {
                log.info("===========================AutoTenderListener===========================");
                log.info("自动投标无匹配规则! borrowId：" + borrowId);
                log.info("========================================================================");
            }

            // 解除锁定
            Borrow updateBorrow = borrowService.findById(borrowId);
            // 在自动投标中, 标的未满.马上将其解除.
            if (!updateBorrow.getMoneyYes().equals(updateBorrow.getMoney())) {
                updateBorrow.setUpdatedAt(nowDate);
                updateBorrow.setIsLock(false);
                updateBorrow.setId(borrowId);
                borrowService.updateById(updateBorrow);
            }
        } catch (Exception e) {
            log.error("自动投标异常 borrowId：" + borrowId, e);
            //取消债权
            jixinTenderRecordHelper.cancelJixinTenderByRedisRecord(borrow.getProductId(), true);
            throw new Exception(e);
        } finally {
            //从redis删除投标申请记录
            jixinTenderRecordHelper.removeJixinTenderRecordInRedis(borrow.getProductId(), true);
        }
    }
}
