package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.FrzFlagContant;
import com.gofobao.framework.award.contants.RedPacketContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoThirdBatchCreditInvest;
import com.google.common.collect.ImmutableMap;
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

import static com.google.common.collect.Maps.newHashMap;

/**
 * Created by Zeke on 2017/5/31.
 */
@Component
@Slf4j
public class BorrowProvider {

    Gson GSON = new GsonBuilder().create();


    @Autowired
    private LendService lendService;
    @Autowired
    private TenderBiz tenderBiz;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private MqHelper mqHelper;

    @Autowired
    private TenderRepository tenderRepository;

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

            //更新借款状态
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);

            Integer borrowType = borrow.getType();
            if ((ObjectUtils.isEmpty(borrow.getPassword())) && (borrowType == 0 || borrowType == 1 || borrowType == 4) && borrow.getApr() > 800) { //判断是否要推送到自动投标队列
                //更新借款状态
                borrow.setIsLock(true);
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
            }
            bool = true;
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
                voCreateTenderReq.setTenderMoney(MathHelper.myRound(borrow.getMoney() / 100.0, 2));
                Map<String, Object> rsMap = tenderBiz.createTender(voCreateTenderReq);

                Object msg = rsMap.get("msg");
                if (ObjectUtils.isEmpty(msg)) {
                    log.error(StringHelper.toString(msg));
                }
            }
            bool = true;
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
            if (ObjectUtils.isEmpty(borrow.getSuccessAt())) {
                borrow.setSuccessAt(new Date());
                borrowService.updateById(borrow);
            }

            ResponseEntity<VoBaseResp> resp = thirdRegisterBorrowAndTender(borrow);
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
                Specification specification = Specifications.<Tender>and()
                        .eq("borrowId", borrowId)
                        .eq("status", TenderConstans.SUCCESS)
                        .build();
                List<Tender> tenders = tenderRepository.findAll(specification);

                //======================================
                // 老用户投标红包
                //======================================
                tenders.stream().forEach(p -> {
                    UserCache userCache = userCacheService.findById(p.getUserId());
                    Map<String, String> paramsMap = newHashMap();
                    MqConfig mqConfig = new MqConfig();
                    // 非新手标  是新手标但是老用投
                    boolean access = (!borrow.getIsNovice()) || (borrow.getIsNovice() && (userCache.getTenderTuijian() || userCache.getTenderQudao()));
                    if (access) {
                        paramsMap.put("type", RedPacketContants.OLD_USER_TENDER_BORROW_REDPACKAGE);
                        paramsMap.put("tenderId", p.getId().toString());
                        paramsMap.put("time", DateHelper.dateToString(new Date()));

                        mqConfig.setMsg(paramsMap);
                        mqConfig.setTag(MqTagEnum.OLD_USER_TENDER);
                        mqConfig.setQueue(MqQueueEnum.RABBITMQ_RED_PACKAGE);
                        mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 120));
                        mqHelper.convertAndSend(mqConfig);
                    }

                    //======================================
                    // 推荐用户投资红包
                    //======================================
                    paramsMap.clear();
                    paramsMap.put("type", RedPacketContants.INVITE_USER_TENDER_BORROW_REDPACKAGE);
                    paramsMap.put("tenderId", p.getId().toString());
                    paramsMap.put("time", DateHelper.dateToString(new Date()));
                    mqConfig.setMsg(paramsMap);
                    mqConfig.setTag(MqTagEnum.INVITE_USER_TENDER);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_RED_PACKAGE);
                    mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 160));
                    mqHelper.convertAndSend(mqConfig);
                });
            } else {
                log.info("====================================================================");
                log.info("非转让标发起复审失败！ msg:" + resp.getBody().getState().getMsg());
                log.info("====================================================================");
            }
        }
        return bool;
    }

    /**
     * 第三方等级标的与债权
     *
     * @param borrow
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> thirdRegisterBorrowAndTender(Borrow borrow) {
        long borrowId = borrow.getId();
        ResponseEntity<VoBaseResp> resp = null;
        String productId = borrow.getProductId();

        if (ObjectUtils.isEmpty(productId)) {
            //标的登记
    /*        int type = borrow.getType();
            if (type != 0 && type != 4) { //判断是否是官标、官标不需要在这里登记标的
                VoCreateThirdBorrowReq voCreateThirdBorrowReq = new VoCreateThirdBorrowReq();
                voCreateThirdBorrowReq.setBorrowId(borrowId);
                resp = borrowThirdBiz.createThirdBorrow(voCreateThirdBorrowReq);
                if (!ObjectUtils.isEmpty(resp)) {
                    return resp;
                }
            }*/
        }

        //批量投标
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowId)
                .eq("isThirdRegister", false)
                .build();
        List<Tender> tenderList = tenderService.findList(ts);
        for (Tender tender : tenderList) {
            VoCreateThirdTenderReq voCreateThirdTenderReq = new VoCreateThirdTenderReq();
            voCreateThirdTenderReq.setAcqRes(String.valueOf(tender.getId()));
            voCreateThirdTenderReq.setUserId(tender.getUserId());
            voCreateThirdTenderReq.setTxAmount(StringHelper.formatDouble(tender.getValidMoney(), 100, false));
            voCreateThirdTenderReq.setProductId(productId);
            voCreateThirdTenderReq.setFrzFlag(FrzFlagContant.FREEZE);
            resp = tenderThirdBiz.createThirdTender(voCreateThirdTenderReq);
            if (!ObjectUtils.isEmpty(resp)) {
                log.error("tenderId:" + tender.getId() + "msg:" + resp.getBody().getState().getMsg());
                return resp;
            }
            tender.setIsThirdRegister(true);
            tenderService.updateById(tender);
        }
        return null;
    }
}
