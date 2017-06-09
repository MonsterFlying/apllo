package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.IntTypeContant;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_lend_pay.BatchLendPayReq;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCreateThirdBorrowReq;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.biz.impl.BorrowRepaymentThirdBizImpl;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchRepay;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.request.VoThirdBatchCreditInvest;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by Zeke on 2017/5/31.
 */
@Component
@Slf4j
public class BorrowProvider {

    Gson GSON = new GsonBuilder().create();

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private LendService lendService;
    @Autowired
    private TenderBiz tenderBiz;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;

    /**
     * 初审
     *
     * @param msg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doFirstVerify(Map<String, String> msg) throws Exception {
        boolean bool = false;
        do {
            Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_BORROW_ID)));
            Borrow borrow = borrowService.findByIdLock(borrowId);
            if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 0)) {
                return false;
            }

            Integer borrowType = borrow.getType();
            if (borrowType == 2) { //秒标
                bool = miaoBorrow(borrow);
            } else if (!ObjectUtils.isEmpty(borrow.getLendId())) { //有草出借
                bool = lendBorrow(borrow);
            } else { //车贷、渠道、净值、转让 标
                bool = baseBorrow(borrow);
            }

        } while (false);
        return bool;
    }

    /**
     * 即信标的登记
     *
     * @param borrow
     * @return
     */
    private boolean createThirdBorrow(Borrow borrow) {
        //===================即信登记标的===========================
        String name = borrow.getName();
        Long userId = borrow.getUserId();

        VoCreateThirdBorrowReq voCreateThirdBorrowReq = new VoCreateThirdBorrowReq();
        voCreateThirdBorrowReq.setUserId(userId);
        voCreateThirdBorrowReq.setRate(StringHelper.formatDouble(borrow.getApr(), 100, false));
        voCreateThirdBorrowReq.setTxAmount(StringHelper.formatDouble(borrow.getMoney(), 100, false));
        voCreateThirdBorrowReq.setAcqRes(String.valueOf(userId));
        voCreateThirdBorrowReq.setIntType(IntTypeContant.SINGLE_USE);
        voCreateThirdBorrowReq.setDuration(String.valueOf(borrow.getTimeLimit()));
        voCreateThirdBorrowReq.setProductDesc(StringUtils.isEmpty(name) ? "净值借款" : name);
        voCreateThirdBorrowReq.setProductId(String.valueOf(borrow.getId()));
        voCreateThirdBorrowReq.setRaiseDate(DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM));
        voCreateThirdBorrowReq.setRaiseEndDate(DateHelper.dateToString(DateHelper.addDays(new Date(), 1), DateHelper.DATE_FORMAT_YMD_NUM));

        ResponseEntity<VoBaseResp> resp = borrowThirdBiz.createThirdBorrow(voCreateThirdBorrowReq);
        if (!ObjectUtils.isEmpty(resp)) {
            log.error("===========================即信标的登记==============================");
            log.error("即信标的登记失败：", resp.getBody().getState().getMsg());
            log.error("=====================================================================");
            return false;
        }
        return true;
    }

    /**
     * 车贷标、净值标、渠道标、转让标初审
     *
     * @return
     */
    private boolean baseBorrow(Borrow borrow) {
        boolean bool = false;
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            Integer borrowType = borrow.getType();
            if ((!ObjectUtils.isEmpty(borrow.getPassword())) || (borrowType != 0 || borrowType != 1 || borrowType != 4) && borrow.getApr() < 800) {
                break;
            }

            //更新借款状态
            borrow.setIsLock(true);
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);

            Date releaseDate = borrow.getReleaseAt();
            //====================================
            //延时投标
            //====================================
            if (borrow.getIsNovice()) {//判断是否是新手标
                Date tempDate = DateHelper.addHours(DateHelper.beginOfDate(new Date()), 20);
                releaseDate = DateHelper.max(tempDate, releaseDate);
            }

            //触发自动投标队列
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_AUTO_TENDER);
            mqConfig.setTag(MqTagEnum.AUTO_TENDER);
            mqConfig.setSendTime(releaseDate);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("borrowProvider autoTender send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Exception e) {
                log.error("borrowProvider autoTender send mq exception", e);
            }

            if (!borrow.isTransfer()) {
                bool = createThirdBorrow(borrow);
            }
        } while (false);
        return bool;
    }

    /**
     * 摘草 生成借款 初审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    private boolean lendBorrow(Borrow borrow) throws Exception {
        boolean bool = false;
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            //更新借款状态
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);

            Long lendId = borrow.getLendId();
            if (!ObjectUtils.isEmpty(lendId)) {
                Lend lend = lendService.findById(lendId);
                VoCreateTenderReq voCreateTenderReq = new VoCreateTenderReq();
                voCreateTenderReq.setUserId(lend.getUserId());
                voCreateTenderReq.setBorrowId(borrow.getId());
                voCreateTenderReq.setTenderMoney(borrow.getMoney());
                Map<String, Object> rsMap = tenderBiz.createTender(voCreateTenderReq);

                Object msg = rsMap.get("msg");
                if (ObjectUtils.isEmpty(msg)) {
                    log.error(StringHelper.toString(msg));
                }
            }
            bool = createThirdBorrow(borrow);
        } while (false);
        return bool;
    }

    /**
     * 秒标初审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    private boolean miaoBorrow(Borrow borrow) throws Exception {
        boolean bool = false;
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            Integer payMoney = 0;
            Integer borrowType = borrow.getType();
            if (borrowType != 2) {
                break;
            }
            Double principal = NumberHelper.toDouble(StringHelper.toString(borrow.getMoney()));
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(principal,
                    NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            payMoney = (int) MathHelper.myRound((Double) rsMap.get("repayTotal") - principal, 2);

            if (borrow.getAwardType() == 1) {
                payMoney += borrow.getAward();
            } else if (borrow.getAwardType() == 2) {  //
                payMoney += (int) MathHelper.myRound(borrow.getMoney() * borrow.getAward() / 100 / 100, 2);
            }

            //更新资产记录
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Frozen);
            entity.setUserId(borrow.getUserId());
            entity.setMoney(payMoney);
            entity.setRemark("冻结秒标应付资金");
            capitalChangeHelper.capitalChange(entity);

            //更新借款状态
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);
            bool = createThirdBorrow(borrow);
        } while (false);
        return bool;
    }

    /**
     * 复审
     *
     * @param msg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doAgainVerify(Map<String, String> msg) throws Exception {
        boolean bool = false;
        Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get("borrowId")));
        Borrow borrow = borrowService.findById(borrowId);
        if (borrow.isTransfer()) { //转让标
            VoThirdBatchCreditInvest voThirdBatchCreditInvest = new VoThirdBatchCreditInvest();
            voThirdBatchCreditInvest.setBorrowId(borrowId);
            ResponseEntity<VoBaseResp> resp = tenderThirdBiz.thirdBatchCreditInvest(voThirdBatchCreditInvest);
            if (ObjectUtils.isEmpty(resp)) {
                log.info("====================================================================");
                log.info("转让标发起复审成功！");
                log.info("====================================================================");
                bool = true;
            } else {
                log.info("====================================================================");
                log.info("转让标发起复审失败！ msg:" + resp.getBody().getState().getMsg());
                log.info("====================================================================");
            }
        } else { //非转让标
            VoThirdBatchLendRepay voThirdBatchLendRepay = new VoThirdBatchLendRepay();
            voThirdBatchLendRepay.setBorrowId(borrowId);
            ResponseEntity<VoBaseResp> resp = borrowRepaymentThirdBiz.thirdBatchLendRepay(voThirdBatchLendRepay);
            if (ObjectUtils.isEmpty(resp)) {
                log.info("====================================================================");
                log.info("非转让标发起复审成功！");
                log.info("====================================================================");
                bool = true;
            } else {
                log.info("====================================================================");
                log.info("非转让标发起复审失败！ msg:" + resp.getBody().getState().getMsg());
                log.info("====================================================================");
            }

        }

        /**
         * @// TODO: 2017/6/2 复审事件
         */
        return bool;
    }
}
