package com.gofobao.framework.repayment.biz.Impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_bail_repay.*;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.batch_details_query.DetailsQueryResp;
import com.gofobao.framework.api.model.batch_lend_pay.*;
import com.gofobao.framework.api.model.batch_repay.*;
import com.gofobao.framework.api.model.batch_repay_bail.*;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.repayment.vo.response.VoBuildThirdRepayResp;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchNoTypeContant;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Zeke on 2017/6/8.
 */
@Service
@Slf4j
public class BorrowRepaymentThirdBizImpl implements BorrowRepaymentThirdBiz {

    final Gson GSON = new GsonBuilder().create();

    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private RepaymentBiz repaymentBiz;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;



    /**
     * 即信批次放款  （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay) throws Exception {
        Date nowDate = new Date();
        Long borrowId = voThirdBatchLendRepay.getBorrowId();
        if (ObjectUtils.isEmpty(borrowId)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "borrowId不存在!"));
        }

        //查询当前借款的所有 状态为1的 tender记录
        Specification<Tender> ts = Specifications.<Tender>and()
                .eq("borrowId", borrowId)
                .eq("status", 1)
                .build();
        List<Tender> tenderList = tenderService.findList(ts);
        if (CollectionUtils.isEmpty(tenderList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "不存在有效的投标记录!"));
        }

        Borrow borrow = borrowService.findById(borrowId);
        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());//收款人存管账户记录
        Long takeUserId = borrow.getTakeUserId();
        if (!ObjectUtils.isEmpty(takeUserId)) {
            takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
        }

        //净值账户管理费
        double fee = 0;
        if (borrow.getType() == 1) {
            double manageFeeRate = 0.0012;
            if (borrow.getRepayFashion() == 1) {
                fee = MathHelper.myRound(borrow.getMoney() * manageFeeRate / 30 * borrow.getTimeLimit(), 2);
            } else {
                fee = MathHelper.myRound(borrow.getMoney() * manageFeeRate * borrow.getTimeLimit(), 2);
            }
        }

        List<LendPay> lendPayList = new ArrayList<>();
        LendPay lendPay = null;
        UserThirdAccount tenderUserThirdAccount = null;
        int sumCount = 0;
        int validMoney = 0;
        int debtFee = 0;
        for (Tender tender : tenderList) {
            debtFee = 0;

            if (!ObjectUtils.isEmpty(tender.getThirdTenderFlag()) && tender.getThirdTenderFlag()) {
                continue;
            }

            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            validMoney = tender.getValidMoney();//投标有效金额
            sumCount += validMoney; //放款总金额

            //添加奖励
            if (borrow.getAwardType() > 0) {
                int money = (int) MathHelper.myRound((tender.getValidMoney() / borrow.getMoney()) * borrow.getAward(), 2);
                if (borrow.getAwardType() == 2) {
                    money = (int) MathHelper.myRound(tender.getValidMoney() * borrow.getAward() / 100, 2);
                }
                debtFee += money;
            }

            //净值账户管理费
            if (borrow.getType() == 1) {
                debtFee += validMoney / borrow.getMoney() * fee;
            }

            String lendPayOrderId = JixinHelper.getOrderId(JixinHelper.LEND_REPAY_PREFIX);

            lendPay = new LendPay();
            lendPay.setAccountId(tenderUserThirdAccount.getAccountId());
            lendPay.setAuthCode(tender.getAuthCode());
            lendPay.setBidFee("0");
            lendPay.setDebtFee(StringHelper.formatDouble(debtFee, 100, false));
            lendPay.setOrderId(lendPayOrderId);
            lendPay.setForAccountId(takeUserThirdAccount.getAccountId());
            lendPay.setTxAmount(StringHelper.formatDouble(validMoney, 100, false));
            lendPay.setProductId(borrow.getProductId());
            lendPayList.add(lendPay);

            tender.setThirdLendPayOrderId(lendPayOrderId);
        }
        tenderService.save(tenderList);

        //记录日志
        String batchNo = jixinHelper.getBatchNo();
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchNoTypeContant.BATCH_LEND_REPAY);
        thirdBatchLog.setRemark("即信批次放款");
        thirdBatchLogService.save(thirdBatchLog);

        BatchLendPayReq request = new BatchLendPayReq();
        request.setBatchNo(batchNo);
        request.setAcqRes(StringHelper.toString(borrowId));//存放borrowId 标id
        request.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/lendrepay/check");
        request.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/lendrepay/run");
        request.setTxAmount(StringHelper.formatDouble(sumCount, 100, false));
        request.setTxCounts(StringHelper.toString(lendPayList.size()));
        request.setSubPacks(GSON.toJson(lendPayList));
        request.setChannel(ChannelContant.HTML);
        BatchLendPayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_LEND_REPAY, request, BatchLendPayResp.class);
        String retCode = response.getRetCode();
        if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.SUCCESS.equals(retCode))) {
            throw new Exception("即信批次放款失败:" + response.getRetMsg());
        }
        if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            throw new Exception("即信批次放款失败!");
        }
        return null;
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayCheckResp repayCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(repayCheckResp)) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("请求体为空!");
        }

        VoThirdBatchRepay voThirdBatchRepay = GSON.fromJson(GSON.toJson(repayCheckResp.getAcqRes()), VoThirdBatchRepay.class);
        if (!JixinResultContants.SUCCESS.equals(repayCheckResp.getRetCode())) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("回调失败! msg:" + repayCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), voThirdBatchRepay.getRepaymentId(),2);
        } else {
            log.info("=============================即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), voThirdBatchRepay.getRepaymentId(),1);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayRunResp repayRunResp = jixinManager.callback(request, new TypeToken<BatchRepayRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(repayRunResp)) {
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(repayRunResp.getRetCode())) {
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("回调失败! msg:" + repayRunResp.getRetMsg());
            bool = false;
        }

        int num = NumberHelper.toInt(repayRunResp.getFailCounts());
        if (num > 0) {
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("即信批次还款处理失败! 一共:" + num + "笔");
            bool = false;
        }

        if (bool) {
            ResponseEntity<VoBaseResp> resp = null;
            try {
                VoRepayReq voRepayReq = GSON.fromJson(repayRunResp.getAcqRes(), new TypeToken<VoRepayReq>() {
                }.getType());
                resp = repaymentBiz.repayDeal(voRepayReq);
            } catch (Throwable e) {
                log.error("还款异常:", e);
            }
            if (ObjectUtils.isEmpty(resp)) {
                log.info("还款成功!");
            }
        } else {
            log.info("还款失败!");
        }
        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchLendPayCheckResp lendRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchLendPayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(lendRepayCheckResp)) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(lendRepayCheckResp.getRetCode())) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("回调失败! msg:" + lendRepayCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), NumberHelper.toLong(lendRepayCheckResp.getAcqRes()),2);
        } else {

            log.info("=============================即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), NumberHelper.toLong(lendRepayCheckResp.getAcqRes()),1);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchLendPayRunResp lendRepayRunResp = jixinManager.callback(request, new TypeToken<BatchLendPayRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(lendRepayRunResp)) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode())) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("回调失败! msg:" + lendRepayRunResp.getRetMsg());
            bool = false;
        }

        //===============================================================
        //处理批次放款失败批次
        //===============================================================
        Long borrowId = NumberHelper.toLong(lendRepayRunResp.getAcqRes());//获取borrowid
        int num = NumberHelper.toInt(lendRepayRunResp.getFailCounts());
        String batchNo = lendRepayRunResp.getBatchNo();//批次号
        do {
            if (num <= 0) {
                break;
            }
            bool = false;

            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("即信批次放款处理结果失败! 一共:" + num + "笔");

            Date nowDate = new Date();

            //0.查询gfb_third_batch_log标获取批次发送时间
            Specification<ThirdBatchLog> tbls = Specifications
                    .<ThirdBatchLog>and()
                    .eq("sourceId", borrowId)
                    .eq("batchNo", batchNo)
                    .build();
            List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
            if (CollectionUtils.isEmpty(thirdBatchLogList)) {
                log.error("即信批次放款回撤：thirdBatchLog记录不存在！");
                break;
            }

            //1.查询批次交易明细
            BatchDetailsQueryReq batchDetailsQueryReq = new BatchDetailsQueryReq();
            batchDetailsQueryReq.setBatchNo(lendRepayRunResp.getBatchNo());
            batchDetailsQueryReq.setBatchTxDate(DateHelper.dateToString(thirdBatchLogList.get(0).getCreateAt(), DateHelper.DATE_FORMAT_YMD_NUM));
            batchDetailsQueryReq.setType("2");
            batchDetailsQueryReq.setPageNum("1");
            batchDetailsQueryReq.setPageSize("10");
            batchDetailsQueryReq.setChannel(ChannelContant.HTML);
            BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, batchDetailsQueryReq, BatchDetailsQueryResp.class);
            if ((ObjectUtils.isEmpty(batchDetailsQueryResp)) || (!JixinResultContants.SUCCESS.equals(batchDetailsQueryResp.getRetCode()))) {
                log.error(ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : batchDetailsQueryResp.getRetMsg());
                break;
            }

            //2.筛选失败批次
            List<String> failureThirdLendPayOrderIds = new ArrayList<>(); //转让标orderId
            List<String> successThirdLendPayOrderIds = new ArrayList<>(); //转让标orderId
            List<DetailsQueryResp> detailsQueryRespList = GSON.fromJson(batchDetailsQueryResp.getSubPacks(), new TypeToken<List<DetailsQueryResp>>() {
            }.getType());
            if (CollectionUtils.isEmpty(detailsQueryRespList)) {
                log.info("================================================================================");
                log.info("即信批次放款处理查询：查询未发现失败批次！");
                log.info("================================================================================");
            }

            Optional<List<DetailsQueryResp>> detailsQueryRespOptional = Optional.of(detailsQueryRespList);
            detailsQueryRespOptional.ifPresent(list -> detailsQueryRespList.forEach(obj -> {
                if ("F".equalsIgnoreCase(obj.getTxState())) {
                    failureThirdLendPayOrderIds.add(obj.getOrderId());
                } else {
                    successThirdLendPayOrderIds.add(obj.getOrderId());
                }
            }));

            if (CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {
                log.info("================================================================================");
                log.info("即信批次放款处理查询：查询未发现失败批次！");
                log.info("================================================================================");
            }

            //失败批次对应债权
            List<Long> borrowIdList = new ArrayList<>();
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdLendPayOrderId", failureThirdLendPayOrderIds.toArray())
                    .build();
            List<Tender> failureTenderList = tenderService.findList(ts);
            for (Tender tender : failureTenderList) {
                borrowIdList.add(tender.getBorrowId());
            }

            //成功批次对应债权
            ts = Specifications
                    .<Tender>and()
                    .in("thirdLendPayOrderId", successThirdLendPayOrderIds.toArray())
                    .build();
            List<Tender> successTenderList = tenderService.findList(ts);
            for (Tender tender : successTenderList) {
                tender.setThirdTenderFlag(true);
            }
            tenderService.save(successTenderList);

            //3.与本地失败投标做匹配，并提出tender
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIdList.toArray())
                    .build();
            List<Borrow> borrowList = borrowService.findList(bs);

            //4.本地资金进行资金解封操作
            int failAmount = 0;//失败金额
            int failNum = 0;//失败次数
            Set<Long> borrowIdSet = new HashSet<>();
            ResponseEntity<VoBaseResp> resp = null;
            for (Borrow borrow : borrowList) {
                failAmount = 0;
                failNum = 0;
                if (!borrowIdSet.contains(borrow.getId())) {
                    for (Tender tender : successTenderList) {
                        if (StringHelper.toString(borrow.getId()).equals(StringHelper.toString(tender.getBorrowId()))) {
                            failAmount += tender.getValidMoney(); //失败金额

                            //================================================
                            //撤销投标申请
                            //================================================
                            VoCancelThirdTenderReq voCancelThirdTenderReq = new VoCancelThirdTenderReq();
                            voCancelThirdTenderReq.setTenderId(tender.getId());
                            resp = tenderThirdBiz.cancelThirdTender(voCancelThirdTenderReq);
                            if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) {
                                return ResponseEntity.ok("error");
                            }

                            //对冻结资金进行回滚
                            tender.setId(tender.getId());
                            tender.setStatus(2); // 取消状态
                            tender.setUpdatedAt(nowDate);

                            CapitalChangeEntity entity = new CapitalChangeEntity();
                            entity.setType(CapitalChangeEnum.Unfrozen);
                            entity.setUserId(tender.getUserId());
                            entity.setMoney(tender.getValidMoney());
                            entity.setRemark("借款 [" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "] 投标与存管通信失败，解除冻结资金。");
                            try {
                                capitalChangeHelper.capitalChange(entity);
                            } catch (Throwable e) {
                                log.error("tenderThirdBizImpl thirdBatchCreditInvestRunCall error：", e);
                            }

                            //可以加上同步资金操作
                        }
                    }
                    borrow.setTenderCount(borrow.getTenderCount() - failNum);
                    borrow.setMoneyYes(borrow.getMoneyYes() - failAmount);
                }
            }
            borrowService.save(borrowList);
            tenderService.save(successTenderList);

            log.error("已对无效投标进行撤回！");
        } while (false);

        if (bool) {
            Borrow borrow = borrowService.findById(borrowId);

            try {
                bool = borrowBiz.notTransferBorrowAgainVerify(borrow);
            } catch (Throwable e) {
                log.error("即信批次放款异常:", e);
            }
            if (bool) {
                log.info("即信批次放款成功!");
            }
        } else {
            log.info("非流转标复审失败!");
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 获取担保人代偿集合
     *
     * @param voBatchBailRepayReq
     * @return
     * @throws Exception
     */
    public List<BailRepay> getBailRepayList(VoBatchBailRepayReq voBatchBailRepayReq) throws Exception {
        Double interestPercent = voBatchBailRepayReq.getInterestPercent();
        Long repaymentId = voBatchBailRepayReq.getRepaymentId();
        interestPercent = ObjectUtils.isEmpty(interestPercent) ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());

        List<BailRepay> bailRepayList = new ArrayList<>();
        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            bailRepayList = new ArrayList<>();
            receivedBailRepay(bailRepayList, borrow, borrowRepayment.getOrder(), interestPercent);
        }
        return bailRepayList;
    }

    /**
     * 担保人代偿
     *
     * @param borrow
     * @param order
     * @param interestPercent
     * @return
     * @throws Exception
     */
    private void receivedBailRepay(List<BailRepay> repayList, Borrow borrow, int order, double interestPercent) throws Exception {
        do {
            //===================================还款校验==========================================
            if (ObjectUtils.isEmpty(borrow)) {
                break;
            }

            Long borrowId = borrow.getId();
            Specification<Tender> specification = Specifications
                    .<Tender>and()
                    .eq("status", 1)
                    .eq("borrowId", borrowId)
                    .build();

            List<Tender> tenderList = tenderService.findList(specification);
            if (CollectionUtils.isEmpty(tenderList)) {
                break;
            }

            List<Long> userIds = new ArrayList<>();
            List<Long> tenderIds = new ArrayList<>();
            for (Tender tender : tenderList) {
                userIds.add(tender.getUserId());
                tenderIds.add(tender.getId());
            }

            Specification<UserCache> ucs = Specifications
                    .<UserCache>and()
                    .in("userId", userIds.toArray())
                    .build();

            List<UserCache> userCacheList = userCacheService.findList(ucs);
            if (CollectionUtils.isEmpty(userCacheList)) {
                break;
            }

            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tenderId", tenderIds.toArray())
                    .eq("status", 0)
                    .eq("order", order)
                    .build();

            List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
            if (CollectionUtils.isEmpty(borrowCollectionList)) {
                break;
            }
            //==================================================================================
            UserThirdAccount tenderUserThirdAccount = null;
            BailRepay bailRepay = null;
            int txFeeIn = 0;//投资方手续费  利息管理费
            int txAmount = 0;//融资人实际付出金额=交易金额+交易利息+还款手续费
            int intAmount = 0;//交易利息
            int principal = 0;
            for (Tender tender : tenderList) {
                bailRepay = new BailRepay();
                txFeeIn = 0;
                intAmount = 0;
                txAmount = 0;
                principal = 0;

                tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());//投标人银行存管账户
                Preconditions.checkNotNull(tenderUserThirdAccount, "投资人存管账户未开户!");
                BorrowCollection borrowCollection = null;//当前借款的回款记录
                for (int i = 0; i < borrowCollectionList.size(); i++) {
                    borrowCollection = borrowCollectionList.get(i);
                    if (StringHelper.toString(tender.getId()).equals(StringHelper.toString(borrowCollection.getTenderId()))) {
                        break;
                    }
                    borrowCollection = null;
                    continue;
                }

                if (tender.getTransferFlag() == 1) {//转让中
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .in("status", 0, 1)
                            .build();

                    List<Borrow> borrowList = borrowService.findList(bs);
                    if (CollectionUtils.isEmpty(borrowList)) {
                        continue;
                    }
                }

                if (tender.getTransferFlag() == 2) { //已转让
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .eq("status", 3)
                            .build();

                    List<Borrow> borrowList = borrowService.findList(bs);
                    if (CollectionUtils.isEmpty(borrowList)) {
                        continue;
                    }

                    Borrow tempBorrow = borrowList.get(0);
                    int tempOrder = order + tempBorrow.getTotalOrder() - borrow.getTotalOrder();
                    //回调
                    receivedBailRepay(repayList, tempBorrow, tempOrder, interestPercent);

                    continue;
                }

                intAmount = (int) (borrowCollection.getInterest() * interestPercent);
                principal = borrowCollection.getPrincipal();

                //收到客户对借款还款
                int interestLower = 0;//应扣除利息
                if (borrow.isTransfer()) {
                    int interest = borrowCollection.getInterest();
                    Date startAt = DateHelper.beginOfDate((Date) borrowCollection.getStartAt().clone());
                    Date collectionAt = DateHelper.beginOfDate((Date) borrowCollection.getCollectionAt().clone());
                    Date endAt = (Date) collectionAt.clone();
                    Date startAtYes = DateHelper.beginOfDate((Date) borrowCollection.getStartAtYes().clone());

                    interestLower = Math.round(interest -
                            interest * Math.max(DateHelper.diffInDays(endAt, startAtYes, false), 0) / DateHelper.diffInDays(collectionAt, startAt, false)
                    );

                    //债权购买人应扣除利息
                    txFeeIn += interestLower;
                }

                //利息管理费
                if (((borrow.getType() == 0) || (borrow.getType() == 4)) && intAmount > interestLower) {
                    /**
                     * '2480 : 好人好梦',1753 : 红运当头',1699 : tasklist',3966 : 苗苗606',1413 : ljc_201',1857 : fanjunle',183 : 54435410',2327 : 栗子',2432 : 高翠西'2470 : sadfsaag',2552 : sadfsaag1',2739 : sadfsaag3',3939 : TinsonCheung',893 : kobayashi',608 : 0211',1216 : zqc9988'
                     */
                    Set<String> stockholder = new HashSet<>(Arrays.asList("2480", "1753", "1699", "3966", "1413", "1857", "183", "2327", "2432", "2470", "2552", "2739", "3939", "893", "608", "1216"));
                    if (!stockholder.contains(tender.getUserId())) {
                        txFeeIn += (int) MathHelper.myRound((intAmount - interestLower) * 0.1, 2);
                    }
                }

                txAmount = principal + intAmount;

                String orderId = JixinHelper.getOrderId(JixinHelper.REPAY_BAIL_PREFIX);
                bailRepay.setOrderId(orderId);
                bailRepay.setTxAmount(StringHelper.formatDouble(txAmount, 100, false));
                bailRepay.setTxCapAmout(StringHelper.formatDouble(principal, 100, false));
                bailRepay.setTxIntAmount(StringHelper.formatDouble(intAmount - txFeeIn, 100, false));
                bailRepay.setOrgOrderId(StringHelper.toString(tender.getThirdTenderOrderId()));
                bailRepay.setOrgTxAmount(StringHelper.formatDouble(tender.getValidMoney(), 100, false));
                bailRepay.setForAccountId(tenderUserThirdAccount.getAccountId());
                repayList.add(bailRepay);

                borrowCollection.setTBailRepayOrderId(orderId);
                borrowCollectionService.updateById(borrowCollection);
            }
        } while (false);
    }

    /**
     * 批次担保账户代偿参数检查回调
     */
    public ResponseEntity<String> thirdBatchBailRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayCheckResp batchBailRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchBailRepayCheckResp>() {
        });
        if (ObjectUtils.isEmpty(batchBailRepayCheckResp)) {
            log.error("=============================批次担保账户代偿参数检查回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(batchBailRepayCheckResp.getRetCode())) {
            log.error("=============================批次担保账户代偿参数检查回调===========================");
            log.error("回调失败! msg:" + batchBailRepayCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), NumberHelper.toLong(batchBailRepayCheckResp.getAcqRes()),2);
        } else {
            log.info("=============================批次担保账户代偿参数成功回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), NumberHelper.toLong(batchBailRepayCheckResp.getAcqRes()),1);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 批次担保账户代偿业务处理回调
     */
    public ResponseEntity<String> thirdBatchBailRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayRunResp batchBailRepayRunResp = jixinManager.callback(request, new TypeToken<BatchBailRepayRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(batchBailRepayRunResp)) {
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(batchBailRepayRunResp.getRetCode())) {
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("回调失败! msg:" + batchBailRepayRunResp.getRetMsg());
            bool = false;
        } else {
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("回调成功!");
        }

        int num = NumberHelper.toInt(batchBailRepayRunResp.getFailCounts());
        if (num > 0) {
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("批次担保账户代偿失败! 一共:" + num + "笔");
            bool = false;
        }

        if (bool) {
            long repaymentId = NumberHelper.toLong(batchBailRepayRunResp.getAcqRes());
            ResponseEntity<VoBaseResp> resp = null;
            try {
                VoAdvanceCall voAdvanceCall = new VoAdvanceCall();
                voAdvanceCall.setRepaymentId(repaymentId);
                voAdvanceCall.setBailRepayRunList(GSON.fromJson(GSON.toJson(batchBailRepayRunResp.getSubPacks()), new TypeToken<List<BailRepayRun>>() {
                }.getType()));
                resp = repaymentBiz.advanceDeal(voAdvanceCall);
            } catch (Throwable e) {
                log.error("垫付异常:", e);
            }
            if (ObjectUtils.isEmpty(resp)) {
                log.info("垫付成功!");
            } else {
                log.error("垫付失败:" + resp.getBody().getState().getMsg());
            }
        }

        return ResponseEntity.ok("success");
    }


    /**
     * 批次融资人还担保账户垫款参数检查回调
     *
     * @param request
     * @param response
     */

    public ResponseEntity<String> thirdBatchRepayBailCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayBailCheckResp batchRepayBailCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayBailCheckResp>() {
        });
        if (ObjectUtils.isEmpty(batchRepayBailCheckResp)) {
            log.error("=============================批次融资人还担保账户垫款参数检查回调===========================");
            log.error("请求体为空!");
        }

        //更新批次状态
        VoBatchRepayBailReq voBatchRepayBailReq = GSON.fromJson(batchRepayBailCheckResp.getAcqRes(), VoBatchRepayBailReq.class);
        if (!JixinResultContants.SUCCESS.equals(batchRepayBailCheckResp.getRetCode())) {
            log.error("=============================批次融资人还担保账户垫款参数检查回调===========================");
            log.error("回调失败! msg:" + batchRepayBailCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(batchRepayBailCheckResp.getBatchNo(), voBatchRepayBailReq.getRepaymentId(),2);
        } else {
            log.error("=============================批次融资人还担保账户垫款参数检查成功===========================");
            thirdBatchLogBiz.updateBatchLogState(batchRepayBailCheckResp.getBatchNo(), voBatchRepayBailReq.getRepaymentId(),1);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 批次融资人还担保账户垫款业务处理回调
     *
     * @param request
     * @param response
     */
    public ResponseEntity<String> thirdBatchRepayBailRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayBailRunResp batchRepayBailRunResp = jixinManager.callback(request, new TypeToken<BatchRepayBailRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(batchRepayBailRunResp)) {
            log.error("=============================批次融资人还担保账户垫款业务处理回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(batchRepayBailRunResp.getRetCode())) {
            log.error("=============================批次融资人还担保账户垫款业务处理回调===========================");
            log.error("回调失败! msg:" + batchRepayBailRunResp.getRetMsg());
            bool = false;
        } else {
            log.error("=============================批次融资人还担保账户垫款业务处理回调===========================");
            log.info("回调成功!");
        }

        int num = NumberHelper.toInt(batchRepayBailRunResp.getFailCounts());
        if (num > 0) {
            log.error("=============================批次融资人还担保账户垫款业务处理回调===========================");
            log.error("批次融资人还担保账户垫款失败! 一共:" + num + "笔");
            bool = false;
        }

        if (bool) {
            try {
                if (bool) {
                    ResponseEntity<VoBaseResp> resp = null;
                    try {
                        VoRepayReq voRepayReq = GSON.fromJson(batchRepayBailRunResp.getAcqRes(), new TypeToken<VoRepayReq>() {
                        }.getType());
                        resp = repaymentBiz.repayDeal(voRepayReq);
                    } catch (Throwable e) {
                        log.error("批次融资人还担保账户垫款异常:", e);
                    }
                    if (ObjectUtils.isEmpty(resp)) {
                        log.info("批次融资人还担保账户垫款成功!");
                    }
                } else {
                    log.info("批次融资人还担保账户垫款失败!");
                }
            } catch (Throwable e) {
                log.error("borrowRepaymentThirdBizImpl 资产变更异常：", e);
            }
        }
        return ResponseEntity.ok("success");
    }

    /**
     * 获取即信还款集合
     *
     * @param voThirdBatchRepay
     * @return
     * @throws Exception
     */
    public List<Repay> getRepayList(VoThirdBatchRepay voThirdBatchRepay) throws Exception {
        int lateInterest = 0;//逾期利息
        Double interestPercent = voThirdBatchRepay.getInterestPercent();
        Long repaymentId = voThirdBatchRepay.getRepaymentId();
        interestPercent = interestPercent == 0 ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        Long borrowId = borrow.getId();//借款ID

        //逾期天数
        Date nowDateOfBegin = DateHelper.beginOfDate(new Date());
        Date repayDateOfBegin = DateHelper.beginOfDate(borrowRepayment.getRepayAt());
        int lateDays = DateHelper.diffInDays(nowDateOfBegin, repayDateOfBegin, false);
        lateDays = lateDays < 0 ? 0 : lateDays;
        if (0 < lateDays) {
            int overPrincipal = borrowRepayment.getPrincipal();
            if (borrowRepayment.getOrder() < (borrow.getTotalOrder() - 1)) { //
                Specification<BorrowRepayment> brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrowId)
                        .eq("status", 0)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                Preconditions.checkNotNull(borrowRepayment, "还款不存在");

                overPrincipal = 0;
                for (BorrowRepayment temp : borrowRepaymentList) {
                    overPrincipal += temp.getPrincipal();
                }
            }
            lateInterest = (int) MathHelper.myRound(overPrincipal * 0.004 * lateDays, 2);
        }

        List<Repay> repayList = new ArrayList<>();
        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            repayList = new ArrayList<>();
            receivedRepay(repayList, borrow, borrowUserThirdAccount.getAccountId(), borrowRepayment.getOrder(), interestPercent, lateDays, lateInterest / 2);
        }
        return repayList;
    }

    /**
     * 获取存管 收到还款 数据集合
     *
     * @param borrow
     * @param order
     * @param interestPercent
     * @param borrowAccountId 借款方即信存管账户id
     * @param lateDays
     * @param lateInterest
     * @return
     * @throws Exception
     */
    public void receivedRepay(List<Repay> repayList, Borrow borrow, String borrowAccountId, int order, double interestPercent, int lateDays, int lateInterest) throws Exception {
        do {
            //===================================还款校验==========================================
            if (ObjectUtils.isEmpty(borrow)) {
                break;
            }

            Long borrowId = borrow.getId();
            Specification<Tender> specification = Specifications
                    .<Tender>and()
                    .eq("status", 1)
                    .eq("borrowId", borrowId)
                    .build();

            List<Tender> tenderList = tenderService.findList(specification);
            if (CollectionUtils.isEmpty(tenderList)) {
                break;
            }

            List<Long> userIds = new ArrayList<>();
            List<Long> tenderIds = new ArrayList<>();
            for (Tender tender : tenderList) {
                userIds.add(tender.getUserId());
                tenderIds.add(tender.getId());
            }

            Specification<UserCache> ucs = Specifications
                    .<UserCache>and()
                    .in("userId", userIds.toArray())
                    .build();

            List<UserCache> userCacheList = userCacheService.findList(ucs);
            if (CollectionUtils.isEmpty(userCacheList)) {
                break;
            }

            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tenderId", tenderIds.toArray())
                    .eq("status", 0)
                    .eq("order", order)
                    .build();

            List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
            if (CollectionUtils.isEmpty(borrowCollectionList)) {
                break;
            }
            //==================================================================================
            UserThirdAccount tenderUserThirdAccount = null;
            Repay repay = null;
            int txFeeIn = 0;//投资方手续费  利息管理费
            int txAmount = 0;//融资人实际付出金额=交易金额+交易利息+还款手续费
            int intAmount = 0;//交易利息
            int txFeeOut = 0;//借款方手续费  逾期利息
            for (Tender tender : tenderList) {
                repay = new Repay();
                txFeeIn = 0;
                txAmount = 0;
                intAmount = 0;
                txFeeOut = 0;

                tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());//投标人银行存管账户
                Preconditions.checkNotNull(tenderUserThirdAccount, "投标人未开户!");
                BorrowCollection borrowCollection = null;//当前借款的回款记录
                for (int i = 0; i < borrowCollectionList.size(); i++) {
                    borrowCollection = borrowCollectionList.get(i);
                    if (StringHelper.toString(tender.getId()).equals(StringHelper.toString(borrowCollection.getTenderId()))) {
                        break;
                    }
                    borrowCollection = null;
                    continue;
                }

                if (tender.getTransferFlag() == 1) {//转让中
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .in("status", 0, 1)
                            .build();

                    List<Borrow> borrowList = borrowService.findList(bs);
                    if (CollectionUtils.isEmpty(borrowList)) {
                        continue;
                    }
                }

                if (tender.getTransferFlag() == 2) { //已转让
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .eq("status", 3)
                            .build();

                    List<Borrow> borrowList = borrowService.findList(bs);
                    if (CollectionUtils.isEmpty(borrowList)) {
                        continue;
                    }

                    Borrow tempBorrow = borrowList.get(0);
                    int tempOrder = order + tempBorrow.getTotalOrder() - borrow.getTotalOrder();
                    int tempLateInterest = tender.getValidMoney() / borrow.getMoney() * lateInterest;

                    //回调
                    receivedRepay(repayList, tempBorrow, borrowAccountId, tempOrder, interestPercent, lateDays, tempLateInterest);
                    continue;
                }

                intAmount = (int) (borrowCollection.getInterest() * interestPercent);
                txAmount = borrowCollection.getPrincipal() + intAmount;

                //收到客户对借款还款
                int interestLower = 0;//应扣除利息
                if (borrow.isTransfer()) {
                    int interest = borrowCollection.getInterest();
                    Date startAt = DateHelper.beginOfDate((Date) borrowCollection.getStartAt().clone());
                    Date collectionAt = DateHelper.beginOfDate((Date) borrowCollection.getCollectionAt().clone());
                    Date startAtYes = DateHelper.beginOfDate((Date) borrowCollection.getStartAtYes().clone());
                    Date endAt = (Date) collectionAt.clone();

                    interestLower = Math.round(interest -
                            interest * Math.max(DateHelper.diffInDays(endAt, startAtYes, false), 0) / DateHelper.diffInDays(collectionAt, startAt, false)
                    );

                    //债权购买人应扣除利息
                    txFeeIn += interestLower;
                }

                //利息管理费
                if (((borrow.getType() == 0) || (borrow.getType() == 4)) && intAmount > interestLower) {
                    /**
                     * '2480 : 好人好梦',1753 : 红运当头',1699 : tasklist',3966 : 苗苗606',1413 : ljc_201',1857 : fanjunle',183 : 54435410',2327 : 栗子',2432 : 高翠西'2470 : sadfsaag',2552 : sadfsaag1',2739 : sadfsaag3',3939 : TinsonCheung',893 : kobayashi',608 : 0211',1216 : zqc9988'
                     */
                    Set<String> stockholder = new HashSet<>(Arrays.asList("2480", "1753", "1699", "3966", "1413", "1857", "183", "2327", "2432", "2470", "2552", "2739", "3939", "893", "608", "1216"));
                    if (!stockholder.contains(tender.getUserId())) {
                        txFeeIn += (int) MathHelper.myRound((intAmount - interestLower) * 0.1, 2);
                    }
                }

                //借款人逾期罚息
                if ((lateDays > 0) && (lateInterest > 0)) {
                    txFeeOut += lateInterest;
                }

                String orderId = JixinHelper.getOrderId(JixinHelper.REPAY_PREFIX);
                repay.setAccountId(borrowAccountId);
                repay.setOrderId(orderId);
                repay.setTxAmount(StringHelper.formatDouble(txAmount + txFeeIn, 100, false));
                repay.setIntAmount(StringHelper.formatDouble(intAmount, 100, false));
                repay.setTxFeeIn(StringHelper.formatDouble(txFeeIn, 100, false));
                repay.setTxFeeOut(StringHelper.formatDouble(txFeeOut, 100, false));
                repay.setProductId(borrow.getProductId());
                repay.setAuthCode(tender.getAuthCode());
                repay.setForAccountId(tenderUserThirdAccount.getAccountId());
                repayList.add(repay);

                borrowCollection.setTRepayOrderId(orderId);
                borrowCollectionService.updateById(borrowCollection);
            }
        } while (false);
    }

}
