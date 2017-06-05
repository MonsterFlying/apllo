package com.gofobao.framework.listener.providers;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.VoFindAutoTenderList;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    public void autoTender(Map<String, String> msg) throws Exception {
        Date nowDate = new Date();
        Long borrowId = NumberHelper.toLong(msg.get(MqConfig.MSG_BORROW_ID));
        do {
            Borrow borrow = borrowService.findByIdLock(borrowId);
            Preconditions.checkNotNull(borrow, "自动投标异常：id为" + borrowId + "借款不存在");

            VoFindAutoTenderList voFindAutoTenderList = new VoFindAutoTenderList();
            List<Map<String, Object>> autoTenderList = null;

            //===========================================================
            int num = 0;
            int pageIndex = 0;
            int maxSize = 50;
            int autoTenderCount = 0; // 中标item
            boolean bool = false;//是否满标
            do {
                pageIndex++;
                voFindAutoTenderList.setPageIndex(pageIndex);
                voFindAutoTenderList.setPageSize(maxSize);
                autoTenderList = autoTenderService.findQualifiedAutoTenders(voFindAutoTenderList);
                if (CollectionUtils.isEmpty(autoTenderList)) {
                    log.info("自动投标MQ：没有匹配到自动投标规则！");
                    break;
                }

                Iterator<Map<String, Object>> itAutoTender = autoTenderList.iterator();
                Map<String, Object> autoTenderMap = null;
                Integer money = 0;
                Integer lowest = 0;
                Integer useMoney = 0;
                Integer borrowMoney = borrow.getMoney();//借款金额（分）
                Integer moneyYes = borrow.getMoneyYes();
                Integer mostAuto = borrow.getMostAuto();
                Set<String> tenderUserIds = new HashSet<>();
                Set<String> autoTenderIds = new HashSet<>();

                AutoTender autoTender = null;
                while (itAutoTender.hasNext()) {//将合格的自动投标  放入消息队列
                    autoTenderMap = itAutoTender.next();

                    if (moneyYes >= borrowMoney || (mostAuto > 0 && moneyYes >= mostAuto)) {
                        bool = true;
                        break;
                    }

                    if (tenderUserIds.contains(StringHelper.toString(autoTenderMap.get("userId")))
                            || autoTenderIds.contains(StringHelper.toString(autoTenderMap.get("id")))) {
                        continue;
                    }

                    useMoney = Integer.parseInt(StringHelper.toString(autoTenderMap.get("useMoney")));
                    money = "1".equals(StringHelper.toString(autoTenderMap.get("mode"))) ? Integer.parseInt(StringHelper.toString(autoTenderMap.get("tenderMoney"))) : useMoney;
                    money = Math.min(money - Integer.parseInt(StringHelper.toString(autoTenderMap.get("saveMoney"))), useMoney);
                    lowest = Integer.parseInt(StringHelper.toString(autoTenderMap.get("lowest")));
                    if ((money < lowest) || ((borrowMoney - moneyYes) < lowest)) {
                        continue;
                    }

                    VoCreateTenderReq voCreateBorrowTender = new VoCreateTenderReq();
                    voCreateBorrowTender.setBorrowId(borrowId);
                    voCreateBorrowTender.setUserId(NumberHelper.toLong(StringHelper.toString(autoTenderMap.get("userId"))));
                    voCreateBorrowTender.setTenderMoney(money);
                    voCreateBorrowTender.setAutoOrder(NumberHelper.toInt(autoTenderMap.get("order")));
                    voCreateBorrowTender.setLowest(NumberHelper.toInt(StringHelper.toString(autoTenderMap.get("lowest"))));
                    voCreateBorrowTender.setIsAutoTender(true);//自动标识

                    Map<String, Object> rs = null;
                    try {
                        if (!tenderUserIds.contains(StringHelper.toString(autoTenderMap.get("userId"))) && !autoTenderIds.contains(StringHelper.toString(autoTenderMap.get("id")))) {
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
                        autoTenderIds.add(StringHelper.toString(autoTenderMap.get("id")));
                        tenderUserIds.add(StringHelper.toString(autoTenderMap.get("userId")));
                        autoTender = new AutoTender();
                        autoTender.setAutoAt(nowDate);
                        autoTender.setId(NumberHelper.toLong(StringHelper.toString(autoTenderMap.get("id"))));
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
        Borrow tempBorrow = new Borrow();
        tempBorrow.setUpdatedAt(nowDate);
        tempBorrow.setIsLock(false);
        tempBorrow.setId(borrowId);
        borrowService.updateById(tempBorrow);
    }

}
