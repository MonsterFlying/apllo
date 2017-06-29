package com.gofobao.framework.listener.providers;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.vo.VoFindAutoTenderList;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.response.VoFindAutoTender;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Created by Zeke on 2017/6/2.
 */
@Component
@Slf4j
public class AutoTenderProvider {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private AutoTenderService autoTenderService;
    @Autowired
    private TenderBiz tenderBiz;

    @Transactional(rollbackFor = Exception.class)
    public void autoTender(Map<String, String> msg) throws Exception {
        Date nowDate = new Date();
        Long borrowId = NumberHelper.toLong(msg.get(MqConfig.MSG_BORROW_ID));
        do {
            Borrow borrow = borrowService.findByIdLock(borrowId);
            Preconditions.checkNotNull(borrow, "自动投标异常：id为" + borrowId + "借款不存在");

            VoFindAutoTenderList voFindAutoTenderList = new VoFindAutoTenderList();
            List<VoFindAutoTender> autoTenderList = null;

            //===========================================================
            int num = 0;
            int pageIndex = 0;
            int maxSize = 50;
            int autoTenderCount = 0; // 中标item
            boolean bool = false;//是否满标
            do {
                pageIndex++;
                voFindAutoTenderList.setStatus("1");
                voFindAutoTenderList.setNotUserId(borrow.getUserId());
                voFindAutoTenderList.setInRepayFashions(countRepayFashions(new Integer[]{borrow.getRepayFashion()}));
                voFindAutoTenderList.setPageIndex(pageIndex);
                voFindAutoTenderList.setPageSize(maxSize);
                voFindAutoTenderList.setBorrowId(borrowId);
                Integer apr = borrow.getApr();
                voFindAutoTenderList.setLtAprFirst(apr);
                voFindAutoTenderList.setGtAprLast(apr);
                autoTenderList = autoTenderService.findQualifiedAutoTenders(voFindAutoTenderList);
                if (CollectionUtils.isEmpty(autoTenderList)) {
                    log.info("自动投标MQ：没有匹配到自动投标规则！");
                    break;
                }

                Iterator<VoFindAutoTender> itAutoTender = autoTenderList.iterator();
                VoFindAutoTender voFindAutoTender = null;
                Integer money = 0;
                Integer lowest = 0;
                Integer useMoney = 0;
                Integer borrowMoney = borrow.getMoney();//借款金额（分）
                Integer moneyYes = borrow.getMoneyYes();
                Integer mostAuto = borrow.getMostAuto();
                Set<Long> tenderUserIds = new HashSet<>();
                Set<Long> autoTenderIds = new HashSet<>();

                AutoTender autoTender = null;
                while (itAutoTender.hasNext()) {//将合格的自动投标  放入消息队列
                    voFindAutoTender = itAutoTender.next();

                    if (moneyYes >= borrowMoney || (mostAuto > 0 && moneyYes >= mostAuto)) {  // 判断是否满标或者 达到自动投标最大额度
                        bool = true;
                        break;
                    }

                    if (tenderUserIds.contains(voFindAutoTender.getUserId())   // 保证每个用户和每个自动投标规则只能使用一次
                            || autoTenderIds.contains(voFindAutoTender.getId())) {
                        continue;
                    }

                    useMoney = voFindAutoTender.getUseMoney();
                    money = voFindAutoTender.getMode() == 1 ? voFindAutoTender.getTenderMoney() : useMoney;
                    money = Math.min(useMoney - voFindAutoTender.getSaveMoney(), money);
                    lowest = voFindAutoTender.getLowest();
                    if ((money < lowest) || ((borrowMoney - moneyYes) < lowest)) {
                        continue;
                    }

                    VoCreateTenderReq voCreateBorrowTender = new VoCreateTenderReq();
                    voCreateBorrowTender.setBorrowId(borrowId);
                    voCreateBorrowTender.setUserId((long)voFindAutoTender.getUserId());
                    voCreateBorrowTender.setTenderMoney(MathHelper.myRound(money / 100.0, 2));
                    voCreateBorrowTender.setAutoOrder(voFindAutoTender.getOrder());
                    voCreateBorrowTender.setLowest(MathHelper.myRound(voFindAutoTender.getLowest() / 100.0, 2));
                    voCreateBorrowTender.setIsAutoTender(true);//自动标识

                    Map<String, Object> rs = null;
                    try {
                        if (!tenderUserIds.contains(voFindAutoTender.getUserId()) && !autoTenderIds.contains(voFindAutoTender.getId())) {
                            rs = tenderBiz.createTender(voCreateBorrowTender);
                        }
                    } catch (Exception e) {
                        log.error("======================================================================");
                        log.error("自动投标MQ：创建自动投标失败", e);
                        log.error("======================================================================");
                    }

                    if (CollectionUtils.isEmpty(rs)) {
                        continue;
                    }

                    Object respMsgState = rs.get("respMsgState");
                    if (ObjectUtils.isEmpty(respMsgState)) {
                        moneyYes += lowest;
                        autoTenderIds.add((long)voFindAutoTender.getId());
                        tenderUserIds.add((long)voFindAutoTender.getUserId());
                        voFindAutoTender.setAutoAt(nowDate);
                        voFindAutoTender.setId(voFindAutoTender.getId());
                        autoTenderService.updateById(autoTender);
                        autoTenderCount++;
                    }
                }

            } while (num < maxSize && !bool);
            if (autoTenderCount >= 1) {
                autoTenderService.updateAutoTenderOrder();
            }

        } while (false);

        //解除锁定
        Borrow borrow = borrowService.findById(borrowId);
        borrow.setUpdatedAt(nowDate);
        borrow.setIsLock(false);
        borrow.setId(borrowId);
        borrowService.updateById(borrow);
        throw new Exception("11");
    }

    /**
     * 计算RepayFashions
     *
     * @param repayFashions borrow表的repayFashion字段
     * @return
     */
    private static String countRepayFashions(Integer[] repayFashions) {
        StringBuffer condition = new StringBuffer();//拼接条件
        StringBuffer binary = null;
        String tempStr = "";
        char[] binaryChar = null;
        for (int i = 1, len = MathHelper.pow(2, 3); i < len; i++) {
            binary = new StringBuffer();
            binaryChar = new char[3];
            tempStr = Integer.toBinaryString(i) + "";
            for (int j = 0; j < 3 - tempStr.length(); j++) {
                binary.append("0");
            }
            binary.append(tempStr).reverse();

            Set<Integer> num = new HashSet<>();
            binary.getChars(0, binary.length(), binaryChar, 0);
            for (Integer repayFashion : repayFashions) {
                if (binaryChar[repayFashion] == '1') {
                    num.add(i);
                }
            }

            for (Integer tempNum : num) {
                condition.append(tempNum).append(",");
            }
        }
        return condition.substring(0, condition.length() - 1);
    }
}
