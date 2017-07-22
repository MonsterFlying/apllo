package com.gofobao.framework.repayment.biz.Impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_bail_repay.BailRepay;
import com.gofobao.framework.api.model.batch_bail_repay.BailRepayRun;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayCheckResp;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayRunResp;
import com.gofobao.framework.api.model.batch_lend_pay.*;
import com.gofobao.framework.api.model.batch_repay.BatchRepayCheckResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayRunResp;
import com.gofobao.framework.api.model.batch_repay.Repay;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailCheckResp;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailRunResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gofobao.framework.helper.DateHelper.isBetween;

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
    private MqHelper mqHelper;
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
        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());// 收款人存管账户记录
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
        LendPay lendPay;
        UserThirdAccount tenderUserThirdAccount;
        double sumCount = 0, validMoney, debtFee;
        for (Tender tender : tenderList) {
            debtFee = 0;
            if (Boolean.TRUE.equals(tender.getThirdTenderFlag())) {
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
                debtFee += MathHelper.myRound(validMoney / new Double(borrow.getMoney()) * fee, 2);
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

        //批次号
        String batchNo = jixinHelper.getBatchNo();
        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("borrowId", borrowId);

        BatchLendPayReq request = new BatchLendPayReq();
        request.setBatchNo(batchNo);
        request.setAcqRes(GSON.toJson(acqResMap));
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

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_LEND_REPAY);
        thirdBatchLog.setRemark("即信批次放款");
        thirdBatchLogService.save(thirdBatchLog);
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
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), voThirdBatchRepay.getRepaymentId(), 2);
        } else {
            log.info("=============================即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), voThirdBatchRepay.getRepaymentId(), 1);
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
        Map<String, Object> acqResMap = GSON.fromJson(repayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        if (ObjectUtils.isEmpty(repayRunResp)) {
            log.error("================================================================================");
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("请求体为空!");
            log.error("================================================================================");
            log.error("================================================================================");
            return ResponseEntity.ok("error");
        }

        if (!JixinResultContants.SUCCESS.equals(repayRunResp.getRetCode())) {
            log.error("================================================================================");
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("回调失败! msg:" + repayRunResp.getRetMsg());
            log.error("================================================================================");
            log.error("================================================================================");
            return ResponseEntity.ok("error");
        } else {
            log.info("================================================================================");
            log.info("=============================即信批次还款处理结果回调===========================");
            log.info("回调成功!");
            log.info("================================================================================");
            log.info("================================================================================");
        }

        //触发处理批次放款处理结果队列
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("repaymentId")), MqConfig.ACQ_RES, repayRunResp.getAcqRes(), MqConfig.BATCH_NO, StringHelper.toString(repayRunResp.getBatchNo()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayRunCall send mq exception", e);
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
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), NumberHelper.toLong(lendRepayCheckResp.getAcqRes()), 2);
        } else {

            log.info("=============================即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), NumberHelper.toLong(lendRepayCheckResp.getAcqRes()), 1);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchLendPayRunResp lendRepayRunResp = jixinManager.callback(request, new TypeToken<BatchLendPayRunResp>() {
        });
        Map<String, Object> acqResMap = GSON.fromJson(lendRepayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        if (ObjectUtils.isEmpty(lendRepayRunResp)) {
            log.error("=========================================批次回调=========================================");
            log.error("=================================即信批次放款处理结果回调=================================");
            log.error("请求体为空!");
            log.error("==========================================================================================");
            log.error("==========================================================================================");
            return ResponseEntity.ok("error");
        }

        if (!JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode())) {
            log.error("=========================================批次回调=========================================");
            log.error("=================================即信批次放款处理结果回调=================================");
            log.error("回调失败! msg:" + lendRepayRunResp.getRetMsg());
            log.error("==========================================================================================");
            log.error("==========================================================================================");
            return ResponseEntity.ok("error");
        } else {
            log.error("=========================================批次回调=========================================");
            log.error("===============================即信批次放款处理结果回调===================================");
            log.error("回调成功!");
            log.error("==========================================================================================");
            log.error("==========================================================================================");
        }

        //触发处理批次放款处理结果队列
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("borrowId")),
                        MqConfig.BATCH_NO, StringHelper.toString(lendRepayRunResp.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchLendRepayRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchLendRepayRunCall send mq exception", e);
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

            List<Long> userIds = tenderList.stream().map(tender -> tender.getUserId()).collect(Collectors.toList());
            List<Long> tenderIds = tenderList.stream().map(tender -> tender.getId()).collect(Collectors.toList());

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
                //当前投资的回款记录
                BorrowCollection borrowCollection = borrowCollectionList.stream().filter(bc -> StringHelper.toString(bc.getTenderId()).equals(StringHelper.toString(tender.getId()))).collect(Collectors.toList()).get(0);

                //判断这笔回款是否已经在即信登记过批次垫付
                if (borrowCollection.getThirdRepayBailFlag()) {
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

                txAmount = principal;

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
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), NumberHelper.toLong(batchBailRepayCheckResp.getAcqRes()), 2);
        } else {
            log.info("=============================批次担保账户代偿参数成功回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), NumberHelper.toLong(batchBailRepayCheckResp.getAcqRes()), 1);
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
            log.error("======================================批次回调======================================");
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("请求体为空!");
            log.error("====================================================================================");
            log.error("====================================================================================");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(batchBailRepayRunResp.getRetCode())) {
            log.error("======================================批次回调======================================");
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("回调失败! msg:" + batchBailRepayRunResp.getRetMsg());
            log.error("====================================================================================");
            log.error("====================================================================================");
            bool = false;
        } else {
            log.error("======================================批次回调======================================");
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("回调成功!");
            log.error("====================================================================================");
            log.error("====================================================================================");
        }

        //=============================================
        // 保存批次担保人代偿授权号
        //=============================================
        List<BailRepayRun> bailRepayRunList = GSON.fromJson(batchBailRepayRunResp.getSubPacks(), new TypeToken<List<BailRepayRun>>() {
        }.getType());
        saveThirdBailRepayAuthCode(bailRepayRunList);

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
     * 保存批次担保人代偿授权号
     *
     * @param bailRepayRunList
     */
    public void saveThirdBailRepayAuthCode(List<BailRepayRun> bailRepayRunList) {
        List<String> orderList = bailRepayRunList.stream().map(bailRepayRun -> bailRepayRun.getOrderId()).collect(Collectors.toList());
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("tBailRepayOrderId", orderList.toArray())
                .build();
        int pageIndex = 0;
        int maxPageSize = 50;
        Pageable pageable = null;
        List<BorrowCollection> borrowCollectionList = null;
        do {
            pageable = new PageRequest(pageIndex++, maxPageSize, new Sort(Sort.Direction.ASC, "id"));
            borrowCollectionList = borrowCollectionService.findList(bcs, pageable);
            if (CollectionUtils.isEmpty(borrowCollectionList)) {
                break;
            }
            Map<String, BorrowCollection> borrowCollectionMap = borrowCollectionList.stream().collect(Collectors.toMap(BorrowCollection::getTBailRepayOrderId, Function.identity()));
            bailRepayRunList.stream().forEach(bailRepayRun -> {
                BorrowCollection borrowCollection = borrowCollectionMap.get(bailRepayRun.getOrderId());
                borrowCollection.setTBailAuthCode(bailRepayRun.getAuthCode());
            });
            borrowCollectionService.save(borrowCollectionList);
        } while (borrowCollectionList.size() >= maxPageSize);
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
            thirdBatchLogBiz.updateBatchLogState(batchRepayBailCheckResp.getBatchNo(), voBatchRepayBailReq.getRepaymentId(), 2);
        } else {
            log.error("=============================批次融资人还担保账户垫款参数检查成功===========================");
            thirdBatchLogBiz.updateBatchLogState(batchRepayBailCheckResp.getBatchNo(), voBatchRepayBailReq.getRepaymentId(), 1);
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
        Map<String, Object> acqResMap = GSON.fromJson(batchRepayBailRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        if (ObjectUtils.isEmpty(batchRepayBailRunResp)) {
            log.error("========================================批次回调============================================");
            log.error("=============================批次融资人还担保账户垫款业务处理回调===========================");
            log.error("请求体为空!");
            log.error("============================================================================================");
            log.error("============================================================================================");
        }

        if (!JixinResultContants.SUCCESS.equals(batchRepayBailRunResp.getRetCode())) {
            log.error("========================================批次回调============================================");
            log.error("=============================批次融资人还担保账户垫款业务处理回调===========================");
            log.error("回调失败! msg:" + batchRepayBailRunResp.getRetMsg());
            log.error("============================================================================================");
            log.error("============================================================================================");
        } else {
            log.error("========================================批次回调============================================");
            log.error("=============================批次融资人还担保账户垫款业务处理回调===========================");
            log.info("回调成功!");
            log.error("============================================================================================");
            log.error("============================================================================================");
        }

        //触发处理批次放款处理结果队列
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("repaymentId")),
                        MqConfig.ACQ_RES, batchRepayBailRunResp.getAcqRes(),
                        MqConfig.BATCH_NO, StringHelper.toString(batchRepayBailRunResp.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayBailRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayBailRunCall send mq exception", e);
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
        interestPercent = ObjectUtils.isEmpty(interestPercent) ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        Long borrowId = borrow.getId();//借款ID

        // 逾期天数
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

                //叠加剩余本金
                overPrincipal = borrowRepaymentList.stream().mapToInt(w -> w.getPrincipal()).sum();
            }
            lateInterest = (int) MathHelper.myRound(overPrincipal * 0.004 * lateDays, 2);
        }

        List<Repay> repayList = new ArrayList<>();
        receivedRepay(repayList, borrow, borrowUserThirdAccount.getAccountId(), borrowRepayment.getOrder(), interestPercent, lateDays, lateInterest / 2);
        return repayList;
    }

    /**
     * 获取存管 收到还款 数据集合
     *
     * @param repayList       还款集合
     * @param borrow          标的
     * @param order           还款期数
     * @param interestPercent 利息比例
     * @param borrowAccountId 借款方即信存管账户id
     * @param lateDays        逾期天数
     * @param lateInterest    逾期利息
     * @return
     * @throws Exception
     */
    public void receivedRepay(List<Repay> repayList, Borrow borrow, String borrowAccountId, int order, double interestPercent, int lateDays, int lateInterest) throws Exception {
        Long borrowId = borrow.getId();
        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowId)
                .build();

        List<Tender> tenderList = tenderService.findList(specification);
        Preconditions.checkNotNull(tenderList, "立即还款: 投标记录为空!");
        Set<Long> userIds = tenderList.stream().map(p -> p.getUserId()).collect(Collectors.toSet());
        List<Long> tenderIds = tenderList.stream().map(p -> p.getId()).collect(Collectors.toList());
        Specification<UserCache> ucs = Specifications
                .<UserCache>and()
                .in("userId", userIds.toArray())
                .build();

        List<UserCache> userCacheList = userCacheService.findList(ucs);
        Preconditions.checkNotNull(userCacheList, "立即还款: 查询用户缓存为空!");
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("order", order)
                .build();

        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Map<Long, BorrowCollection> borrowCollectionMap = borrowCollectionList
                .stream()
                .collect(Collectors.toMap(BorrowCollection::getTenderId,
                        Function.identity()));
        Preconditions.checkNotNull(borrowCollectionList, "立即还款: 查询还款记录为空!");

        UserThirdAccount tenderUserThirdAccount;
        Repay repay;
        //融资人实际付出金额 = 交易金额 + 交易利息 + 还款手续费
        int txFeeIn = 0;    // 投资方手续费 利息管理费
        int txAmount = 0;   // 还款本金
        int intAmount = 0;  // 交易利息
        int txFeeOut = 0;   // 借款方手续费  逾期利息
        for (Tender tender : tenderList) {
            repay = new Repay();
            txFeeIn = 0;
            txFeeOut = 0;

            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId()); // 投标人银行存管账户
            Preconditions.checkNotNull(tenderUserThirdAccount, "投标人未开户!");
            BorrowCollection borrowCollection = borrowCollectionMap.get(tender.getId());  // 还款计划
            Preconditions.checkNotNull(borrowCollection, "立即还款: 根据投标记录查询还款记录查询为空");

            //==============================================================
            // 判断还款是否已经在即信登记
            //==============================================================
            if (borrowCollection.getThirdRepayFlag()) {
                continue;
            }

            if (tender.getTransferFlag() == 1) {   //转让中
                Specification<Borrow> bs = Specifications
                        .<Borrow>and()
                        .eq("tenderId", tender.getId())
                        .in("status", 0, 1)
                        .build();

                List<Borrow> borrowList = borrowService.findList(bs);
                if (!CollectionUtils.isEmpty(borrowList)) {
                    VoCancelBorrow voCancelBorrow = new VoCancelBorrow();
                    voCancelBorrow.setBorrowId(borrowList.get(0).getId());
                    //取消当前借款
                    borrowBiz.cancelBorrow(voCancelBorrow);

                }
                tender.setTransferFlag(0);//设置转让标识
            }

            if (tender.getTransferFlag() == 2) { //已转让
                Specification<Borrow> bs = Specifications
                        .<Borrow>and()
                        .eq("tenderId", tender.getId())
                        .eq("status", 3)
                        .build();
                List<Borrow> borrowList = borrowService.findList(bs);
                Preconditions.checkNotNull(borrowList, "查询转让标的为空");
                Borrow tempBorrow = borrowList.get(0);

                int tempOrder = order + tempBorrow.getTotalOrder() - borrow.getTotalOrder();
                int tempLateInterest = tender.getValidMoney() / borrow.getMoney() * lateInterest;  // 逾期收入
                receivedRepay(repayList, tempBorrow, borrowAccountId, tempOrder, interestPercent, lateDays, tempLateInterest); //递归处理
                continue;
            }

            intAmount = new Double(borrowCollection.getInterest() * interestPercent).intValue();  // 本期还款利息
            txAmount = borrowCollection.getPrincipal();  // 本期还款金额

            // 此处代码说明
            // 还款计划:2.1号, 3.1号, 4.1号. 5.1号, 6.1号;
            // 其中2.1号, 3.1号 已经还款, 当在3.15 发生债权转让
            // 其中3.1 - 3.15 的利息本应该属于原出借人所有.因为存管平台的限制;
            // 导致部分利息被规划到新的债权承接人手里.跟平台业务不符合, 所以使用一下方案:
            // 还款时: 直接使用手续费形式扣除新承接人的该部分利息.然后通过红包形式返还给原债权出借人该部分利息
            int interestLower = 0;  // 应扣除利息
            if (borrow.isTransfer()) {
                int interest = borrowCollection.getInterest();
                Date startAt = DateHelper.beginOfDate(borrowCollection.getStartAt());
                Date collectionAt = DateHelper.beginOfDate(borrowCollection.getCollectionAt());
                Date startAtYes = DateHelper.beginOfDate(borrowCollection.getStartAtYes());
                interestLower = Math.round(interest -
                        interest * Math.max(DateHelper.diffInDays(collectionAt, startAtYes, false), 0)
                                / DateHelper.diffInDays(collectionAt, startAt, false)
                );

                //  债权购买人应扣除利息
                txFeeIn += interestLower;  // 收款人利息
            }

            //  平台收取出借用户利息管理费为: 利息的百分之十;
            //  特殊注意: 其中有部分用户不需要收取手续费(在2015年签署股东写).有效期(2015. 12 - 2017.12.25)

            if (((borrow.getType() == 0) || (borrow.getType() == 4)) && intAmount > interestLower) {
                ImmutableSet<Long> stockholder = ImmutableSet.of(2480L, 1753L, 1699L,
                        3966L, 1413L, 1857L,
                        183L, 2327L, 2432L,
                        2470L, 2552L, 2739L,
                        3939L, 893L, 608L,
                        1216L);

                boolean between = isBetween(new Date(), DateHelper.stringToDate("2015-12-25 00:00:00"),
                        DateHelper.stringToDate("2017-12-25 23:59:59"));
                if ((stockholder.contains(tender.getUserId())) && (between)) {
                    txFeeIn += 0;
                } else {
                    txFeeIn += new Double(MathHelper.myRound((intAmount - interestLower) * 0.1, 2)).intValue();
                }
            }

            //借款人逾期罚息
            if ((lateDays > 0) && (lateInterest > 0)) {
                txFeeOut += tender.getValidMoney().doubleValue() / borrow.getMoney().doubleValue() * lateInterest;
            }

            String orderId = JixinHelper.getOrderId(JixinHelper.REPAY_PREFIX);
            repay.setAccountId(borrowAccountId);
            repay.setOrderId(orderId);
            repay.setTxAmount(StringHelper.formatDouble(txAmount, 100, false));
            repay.setIntAmount(StringHelper.formatDouble(intAmount, 100, false));
            repay.setTxFeeIn(StringHelper.formatDouble(txFeeIn, 100, false));
            repay.setTxFeeOut(StringHelper.formatDouble(txFeeOut, 100, false));
            repay.setProductId(borrow.getProductId());
            repay.setAuthCode(tender.getAuthCode());
            repay.setForAccountId(tenderUserThirdAccount.getAccountId());
            repayList.add(repay);

            borrowCollection.setTRepayOrderId(orderId);
            borrowCollectionService.updateById(borrowCollection);

            //更新投标
            tenderService.updateById(tender);
        }
    }

}
