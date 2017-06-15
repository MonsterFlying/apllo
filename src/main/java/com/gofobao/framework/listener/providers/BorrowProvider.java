package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.FrzFlagContant;
import com.gofobao.framework.api.model.debt_details_query.DebtDetail;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResp;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCreateThirdBorrowReq;
import com.gofobao.framework.borrow.vo.request.VoQueryThirdBorrowList;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoThirdBatchCreditInvest;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private TenderService tenderService;

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

            if (!ObjectUtils.isEmpty(borrow.getLendId())) { //有草出借
                bool = lendBorrow(borrow);
            } else { //车贷、渠道、净值、转让 标
                bool = baseBorrow(borrow);
            }

        } while (false);
        return bool;
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
            //mqConfig.setSendTime(releaseDate);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("borrowProvider autoTender send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Exception e) {
                log.error("borrowProvider autoTender send mq exception", e);
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
        if (borrow.getStatus() != 1) {
            log.error("复审：借款状态已发生改变！");
            return false;
        }

        if (borrow.isTransfer()) {
            //批次债券转让
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

            VoQueryThirdBorrowList voQueryThirdBorrowList = new VoQueryThirdBorrowList();
            voQueryThirdBorrowList.setBorrowId(borrowId);
            voQueryThirdBorrowList.setUserId(borrow.getUserId());
            voQueryThirdBorrowList.setPageNum("1");
            voQueryThirdBorrowList.setPageSize("10");
            DebtDetailsQueryResp response = borrowThirdBiz.queryThirdBorrowList(voQueryThirdBorrowList);

            List<DebtDetail> debtDetailList = GSON.fromJson(response.getSubPacks(), new TypeToken<List<DebtDetail>>() {
            }.getType());

            ResponseEntity<VoBaseResp> resp = null;
            if (debtDetailList.size() < 1) {
                resp = thirdRegisterBorrowAndTender(borrowId);
            }
            if (ObjectUtils.isEmpty(resp)) {
                //批次放款
                VoThirdBatchLendRepay voThirdBatchLendRepay = new VoThirdBatchLendRepay();
                voThirdBatchLendRepay.setBorrowId(borrowId);
                resp = borrowRepaymentThirdBiz.thirdBatchLendRepay(voThirdBatchLendRepay);
            }

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

    /**
     * 第三方等级标的与债权
     *
     * @param borrowId
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdRegisterBorrowAndTender(Long borrowId) {
        //标的登记
        VoCreateThirdBorrowReq voCreateThirdBorrowReq = new VoCreateThirdBorrowReq();
        voCreateThirdBorrowReq.setBorrowId(borrowId);
        ResponseEntity<VoBaseResp> resp = borrowThirdBiz.createThirdBorrow(voCreateThirdBorrowReq);
        if (!ObjectUtils.isEmpty(resp)) {
            return resp;
        }
        //批量投标
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowId)
                .build();
        List<Tender> tenderList = tenderService.findList(ts);
        for (Tender tender : tenderList) {
            VoCreateThirdTenderReq voCreateThirdTenderReq = new VoCreateThirdTenderReq();
            voCreateThirdTenderReq.setAcqRes(String.valueOf(tender.getId()));
            voCreateThirdTenderReq.setUserId(tender.getUserId());
            voCreateThirdTenderReq.setTxAmount(StringHelper.formatDouble(tender.getValidMoney(), 100, false));
            voCreateThirdTenderReq.setProductId(String.valueOf(borrowId));
            voCreateThirdTenderReq.setFrzFlag(FrzFlagContant.FREEZE);
            resp = tenderThirdBiz.createThirdTender(voCreateThirdTenderReq);
            if (!ObjectUtils.isEmpty(resp)) {
                log.error("tenderId:" + tender.getId() + "msg:" + resp.getBody().getState().getMsg());
                return resp;
            }
        }

        return null;
    }
}
