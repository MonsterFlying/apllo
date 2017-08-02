package com.gofobao.framework.listener.providers;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.vo.VoFindAutoTenderList;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    /**
     * 自动投标
     *
     * @param msg
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoTender(Map<String, String> msg) throws Exception {
        Date nowDate = new Date();
        Long borrowId = NumberHelper.toLong(msg.get(MqConfig.MSG_BORROW_ID));
        Borrow borrow = borrowService.findByIdLock(borrowId);
        Preconditions.checkNotNull(borrow, "自动投标异常：id为" + borrowId + "借款不存在");
        VoFindAutoTenderList voFindAutoTenderList = new VoFindAutoTenderList();
        List<Map<String, Object>> autoTenderList = null;
        long moneyYes = borrow.getMoneyYes();

        int num = 0;
        int pageIndex = 0;
        int maxSize = 50;
        int autoTenderCount = 0; // 中标item
        boolean bool = false;//是否满标
        do {
            pageIndex++;
            voFindAutoTenderList.setStatus("1");
            voFindAutoTenderList.setNotUserId(borrow.getUserId());
            voFindAutoTenderList.setInRepayFashions(BorrowHelper.countRepayFashions(new Integer[]{borrow.getRepayFashion()}));
            voFindAutoTenderList.setPageIndex(pageIndex);
            voFindAutoTenderList.setPageSize(maxSize);
            voFindAutoTenderList.setBorrowId(borrowId);
            Integer apr = borrow.getApr();
            voFindAutoTenderList.setLtAprFirst(apr);
            voFindAutoTenderList.setGtAprLast(apr);
            autoTenderList = autoTenderService.findQualifiedAutoTenders(voFindAutoTenderList);  // 查询自动投标队列
            if (CollectionUtils.isEmpty(autoTenderList)) {
                log.info("自动投标MQ：第" + (pageIndex + 1) + "页,没有匹配到自动投标规则！");
                break;
            }

            Iterator<Map<String, Object>> itAutoTender = autoTenderList.iterator();
            Map<String, Object> voFindAutoTender = null;
            long money = 0;
            long lowest = 0;
            long useMoney = 0;
            Integer borrowMoney = borrow.getMoney(); // 借款金额（分）
            Integer mostAuto = borrow.getMostAuto();
            Set<Long> tenderUserIds = new HashSet<>();
            Set<Long> autoTenderIds = new HashSet<>();
            AutoTender autoTender = null;
            while (itAutoTender.hasNext()) { // 将合格的自动投标  放入消息队列
                autoTender = new AutoTender();
                voFindAutoTender = itAutoTender.next();
                if ((moneyYes >= borrowMoney) || (mostAuto > 0 && moneyYes >= mostAuto)) {  // 判断是否满标或者 达到自动投标最大额度
                    bool = true;
                    break;
                }

                if (tenderUserIds.contains(NumberHelper.toLong(voFindAutoTender.get("userId")))   // 保证每个用户 和 每个自动投标规则只能使用一次
                        || autoTenderIds.contains(NumberHelper.toLong(voFindAutoTender.get("id")))) {
                    continue;
                }

                useMoney = NumberHelper.toLong(voFindAutoTender.get("useMoney"));  // 用户可用金额
                money = String.valueOf(voFindAutoTender.get("mode")).equals("1") ? NumberHelper.toInt(voFindAutoTender.get("tenderMoney")) : useMoney;
                money = Math.min(useMoney - NumberHelper.toInt(voFindAutoTender.get("saveMoney")), money);
                lowest = NumberHelper.toLong(voFindAutoTender.get("lowest")); // 最小投标金额
                if ((money < lowest)) {
                    continue;
                }

                // 标的金额小于 最小投标金额
                if (borrowMoney - moneyYes < lowest) {
                    continue;
                }

                VoCreateTenderReq voCreateBorrowTender = new VoCreateTenderReq();
                voCreateBorrowTender.setBorrowId(borrowId); // 标的
                voCreateBorrowTender.setUserId(NumberHelper.toLong(voFindAutoTender.get("userId"))); // 投标用户
                voCreateBorrowTender.setTenderMoney(MathHelper.myRound(money / 100.0, 2));  // 投标金额
                voCreateBorrowTender.setAutoOrder(NumberHelper.toInt(voFindAutoTender.get("order")));
                voCreateBorrowTender.setIsAutoTender(true);//自动标识

                if ((!tenderUserIds.contains(NumberHelper.toLong(voFindAutoTender.get("userId"))))
                        && (!autoTenderIds.contains(NumberHelper.toLong(voFindAutoTender.get("id"))))) {  // 保证自动不能重复
                    ResponseEntity<VoBaseResp> response = tenderBiz.createTender(voCreateBorrowTender);
                    if (response.getStatusCode().equals(HttpStatus.OK)) {
                        moneyYes += lowest;
                        autoTenderIds.add(NumberHelper.toLong(voFindAutoTender.get("id")));
                        tenderUserIds.add(NumberHelper.toLong(voFindAutoTender.get("userId")));
                        autoTender.setAutoAt(nowDate);
                        autoTenderService.updateById(autoTender);
                        autoTenderCount++;
                    } else {
                        continue;
                    }
                }
            }
        } while (num < maxSize && !bool);
        if (autoTenderCount >= 1) {
            autoTenderService.updateAutoTenderOrder();
        }

        // 解除锁定
        Borrow updateBorrow = borrowService.findById(borrowId);
        if (!updateBorrow.getMoneyYes().equals(updateBorrow.getMoney())) { // 在自动投标中, 标的未满.马上将其解除.
            updateBorrow.setUpdatedAt(nowDate);
            updateBorrow.setIsLock(false);
            updateBorrow.setId(borrowId);
            borrowService.updateById(updateBorrow);
        }
    }


}
