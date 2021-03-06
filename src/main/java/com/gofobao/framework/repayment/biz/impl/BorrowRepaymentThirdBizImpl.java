package com.gofobao.framework.repayment.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeReq;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeResp;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayCheckResp;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayRunResp;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvestRun;
import com.gofobao.framework.api.model.batch_lend_pay.*;
import com.gofobao.framework.api.model.batch_query.BatchQueryReq;
import com.gofobao.framework.api.model.batch_query.BatchQueryResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayCheckResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayRunResp;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.contract.biz.ContractBiz;
import com.gofobao.framework.contract.contants.ContractContants;
import com.gofobao.framework.contract.entity.BorrowContract;
import com.gofobao.framework.contract.repository.BorrowContractRepository;
import com.gofobao.framework.contract.service.ContractService;
import com.gofobao.framework.contract.vo.request.BindBorrow;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.TrusteePayQueryHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.contants.ThirdDealStatusContrants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchDealLogContants;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoEndTransfer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/6/8.
 */
@Service
@Slf4j
public class BorrowRepaymentThirdBizImpl implements BorrowRepaymentThirdBiz {

    final Gson GSON = new GsonBuilder().create();
    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;

    @Autowired
    private JixinManager jixinManager;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private JixinHelper jixinHelper;

    @Autowired
    private BorrowCollectionService borrowCollectionService;

    @Autowired
    private ThirdBatchLogService thirdBatchLogService;

    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;

    @Autowired
    private AssetChangeProvider assetChangeProvider;

    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferBuyLogService transferBuyLogService;

    @Autowired
    private TransferBiz transferBiz;

    @Autowired
    private BatchAssetChangeService batchAssetChangeService;

    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;

    @Autowired
    private ThirdBatchDealLogBiz thirdBatchDealLogBiz;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private ExceptionEmailHelper exceptionEmailHelper;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;
    @Autowired
    private MqHelper mqHelper;

    @Autowired
    TrusteePayQueryHelper trusteePayQueryHelper;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractBiz contractBiz;

    /**
     * 非流转标的 即信批次放款 （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay) throws Exception {
        Gson gson = new Gson();
        log.info(String.format("批次放款调用: %s", gson.toJson(voThirdBatchLendRepay)));
        Date nowDate = new Date();
        Long borrowId = voThirdBatchLendRepay.getBorrowId();
        //查询当前借款的所有 状态为1的 tender记录
        Specification<Tender> ts = Specifications.<Tender>and()
                .eq("borrowId", borrowId)
                .eq("status", 1)
                .build();

        List<Tender> tenderList = tenderService.findList(ts);
        Preconditions.checkState(!CollectionUtils.isEmpty(tenderList), "批次放款调用: 投标记录为空");

        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "批次放款调用: 标的信息为空 ");

        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());// 收款人存管账户记录
        Preconditions.checkNotNull(takeUserThirdAccount, "借款人未开户!");

        /*查询受托支付是否成功*/
        TrusteePayQueryResp trusteePayQueryResp = trusteePayQueryHelper.queryTrusteePayQuery(takeUserThirdAccount.getAccountId(),
                borrow.getProductId(),
                4);

        if (ObjectUtils.isEmpty(trusteePayQueryResp)) {
            throw new Exception("批次放款调用：受托支付查询失败,msg->" + trusteePayQueryResp.getRetMsg());
        }

        if ("1".equals(trusteePayQueryResp.getState())) {
            Long takeUserId = borrow.getTakeUserId();  //收款人id
            if (!ObjectUtils.isEmpty(takeUserId)) {
                takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
            }
        }

        long totalManageFee = 0; // 信用标, 收取账户管理费
        if (borrow.getType() == 1) {
            double manageFeeRate = 0.0012;
            if (borrow.getRepayFashion() == 1) {
                totalManageFee = new Double(MoneyHelper.round(borrow.getMoney() * manageFeeRate / 30 * borrow.getTimeLimit(), 0)).longValue();
            } else {
                totalManageFee = new Double(MoneyHelper.round(borrow.getMoney() * manageFeeRate * borrow.getTimeLimit(), 0)).longValue();
            }
        }

        List<LendPay> lendPayList = new ArrayList<>();
        LendPay lendPay;
        UserThirdAccount tenderUserThirdAccount;
        double sumTxAmount = 0, validMoney, debtFee;
        double sumNetWorthFee = 0;
        for (Tender tender : tenderList) {
            debtFee = 0;
            //投标有效金额
            validMoney = tender.getValidMoney();
            /*信用管理费*/
            long newWorthFee = new Double(MoneyHelper.round(MoneyHelper.multiply(MoneyHelper.divide(validMoney, borrow.getMoney()), totalManageFee), 0)).longValue();
            //信用账户管理费
            if (borrow.getType() == 1) {
                sumNetWorthFee = MoneyHelper.add(sumNetWorthFee, newWorthFee);
            }
            //已经处理过的批次不放即信处理
            if (BooleanHelper.isTrue(tender.getThirdTenderFlag())) {
                continue;
            }
            //即信收取信用账户管理费
            if (borrow.getType() == 1) {
                debtFee = MoneyHelper.add(debtFee, newWorthFee);
            }
            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            Preconditions.checkNotNull(tenderUserThirdAccount, "投资人未开户!");

            sumTxAmount += validMoney; //放款总金额

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
            tender.setUpdatedAt(nowDate);
        }
        tenderService.save(tenderList);
        String batchNo = jixinHelper.getBatchNo();  // 批次放款
        /**
         * 绑定标的合同
         */
        //TODO 借款人是否委托授权
        /*UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());
        if (!ObjectUtils.isEmpty(userThirdAccount)
                && !StringUtils.isEmpty(userThirdAccount.getOpenAccountAt())
                && userThirdAccount.getEntrustState()) {
            try {
                log.info("============进入标的绑定合同==============");
                BindBorrow bindBorrow = new BindBorrow();
                bindBorrow.setProductId(borrowId);
                bindBorrow.setTemplateId(140);
                bindBorrow.setCustomField("");
                bindBorrow.setTradeType(ContractContants.BORROWING);
                contractBiz.debtTemplate(bindBorrow);
                List<BorrowContract> borrowContracts = new ArrayList<>();
                tenderList.forEach(p -> {
                    Long tenderUserId = p.getUserId();
                    UserThirdAccount userThirdAccount1 = userThirdAccountService.findByUserId(tenderUserId);
                    if (!ObjectUtils.isEmpty(userThirdAccount1)
                            && !StringUtils.isEmpty(userThirdAccount.getOpenAccountAt())
                            && userThirdAccount1.getEntrustState()) {
                        BorrowContract borrowContract = new BorrowContract();
                        borrowContract.setBatchNo(batchNo);
                        borrowContract.setUserId(borrow.getUserId());
                        borrowContract.setBorrowId(p.getBorrowId());
                        borrowContract.setType(1);
                        borrowContract.setBorrowName(borrow.getName());
                        borrowContract.setForUserId(tenderUserId);
                        borrowContract.setCreatedAt(p.getCreatedAt());
                        borrowContract.setUpdateAt(new Date());
                        borrowContracts.add(borrowContract);
                    }
                });
                if (!CollectionUtils.isEmpty(borrowContracts)) {
                    contractService.bitchSave(borrowContracts);
                }
            } catch (Exception e) {
                log.error("绑定标的合同失败", e);
            }
        }*/

        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("borrowId", borrowId);
        String data = "";
        BatchLendPayReq batchLendPayReq = new BatchLendPayReq();

        try {
            batchLendPayReq.setBatchNo(batchNo);
            batchLendPayReq.setAcqRes(GSON.toJson(acqResMap));
            batchLendPayReq.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/lendrepay/check");
            batchLendPayReq.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/lendrepay/run");
            batchLendPayReq.setTxAmount(StringHelper.formatDouble(sumTxAmount, 100, false));
            batchLendPayReq.setTxCounts(StringHelper.toString(lendPayList.size()));
            batchLendPayReq.setSubPacks(GSON.toJson(lendPayList));
            // BatchLendPayResp batchLendPayResp = jixinManager.send(JixinTxCodeEnum.BATCH_LEND_REPAY, batchLendPayReq, BatchLendPayResp.class);
            BatchLendPayResp batchLendPayResp = safetyLend(batchLendPayReq);  // 安全放款
            data = gson.toJson(batchLendPayReq);
            log.info("==============================");
            log.info(String.format("批次放款请求， 数据[%s]", data));
            log.info("==============================");

            if ((ObjectUtils.isEmpty(batchLendPayResp))
                    || (!JixinResultContants.SUCCESS.equalsIgnoreCase(batchLendPayResp.getRetCode()))) {
                log.error(String.format("批次放款失败, 请求[%s], 响应[%s]",
                        GSON.toJson(batchLendPayReq),
                        GSON.toJson(batchLendPayResp)));

                exceptionEmailHelper.sendErrorMessage("批次放款失败",
                        String.format("请求: [%s], 响应: [%s]",
                                GSON.toJson(batchLendPayReq),
                                GSON.toJson(batchLendPayResp)));

                throw new Exception("请求存管放款失败");
            }

            //新增放款资产变动记录
            addBorrowLendRepayAssetChange(nowDate, borrowId, borrow, sumNetWorthFee, batchNo);
            //将新手投标用户置为已投状态
            recordUserNoviceTender(borrow, tenderList);

            //记录日志
            ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
            thirdBatchLog.setBatchNo(batchNo);
            thirdBatchLog.setCreateAt(nowDate);
            thirdBatchLog.setTxDate(batchLendPayReq.getTxDate());
            thirdBatchLog.setTxTime(batchLendPayReq.getTxTime());
            thirdBatchLog.setSeqNo(batchLendPayReq.getSeqNo());
            thirdBatchLog.setUpdateAt(nowDate);
            thirdBatchLog.setSourceId(borrowId);
            thirdBatchLog.setType(ThirdBatchLogContants.BATCH_LEND_REPAY);
            thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
            thirdBatchLog.setRemark("即信批次放款");
            thirdBatchLog = thirdBatchLogService.save(thirdBatchLog);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(thirdBatchLog.getBatchNo(), thirdBatchLog.getSourceId(),
                    ThirdBatchDealLogContants.SEND_REQUEST, true, ThirdBatchLogContants.BATCH_LEND_REPAY, "");

            //改变批次放款状态 处理中
            borrow.setLendRepayStatus(ThirdDealStatusContrants.DISPOSING);
            borrowService.save(borrow);
            return ResponseEntity.ok(VoBaseResp.ok("放款申请成功!"));
        } catch (Exception e) {
            log.error("放款失败异常", e);
            safeCancelLend(batchLendPayReq); // 安全取消批次
            throw new Exception(e);
        }
    }


    /**
     * 安全放款
     * <p>此处会调用即信批次查询接口, 判断是否存在该放款批次记录</p>
     * <p>如果查询到该批次, 直接返回放款成功</p>
     * <p>如果查询不到该批次, 执行批次放款</p>
     *
     * @param batchLendPayReq
     * @return
     */
    private BatchLendPayResp safetyLend(BatchLendPayReq batchLendPayReq) {
        BatchQueryResp batchQueryResp = queryBatchQuery(batchLendPayReq.getTxDate(), batchLendPayReq.getBatchNo(), 4);   // 先查询即信
        if ((ObjectUtils.isEmpty(batchQueryResp))
                || (!JixinResultContants.SUCCESS.equalsIgnoreCase(batchQueryResp.getRetCode()))) {
            log.error(String.format("批次查询, 数据[%s]", GSON.toJson(batchLendPayReq)));
            return doLend(batchLendPayReq, 4);
        } else {
            log.info(String.format("即信批次重复放款, TxDate: %s, batchNo:%s ", batchLendPayReq.getTxDate(), batchLendPayReq.getBatchNo()));
            BatchLendPayResp batchLendPayResp = new BatchLendPayResp();
            batchLendPayResp.setRetCode(JixinResultContants.SUCCESS);
            batchLendPayResp.setRetMsg("成功");
            batchLendPayResp.setBatchNo(batchLendPayReq.getBatchNo());
            batchLendPayResp.setNotifyURL(batchLendPayReq.getNotifyURL());
            batchLendPayResp.setRetNotifyURL(batchLendPayReq.getRetNotifyURL());
            batchLendPayResp.setSubPacks(batchLendPayReq.getSubPacks());
            batchLendPayResp.setTxAmount(batchLendPayReq.getTxAmount());
            batchLendPayResp.setTxCounts(batchLendPayReq.getTxCounts());
            return batchLendPayResp;
        }
    }

    /**
     * 实际申报放款, 会有重试机制
     *
     * @param batchLendPayReq
     * @param retryNum
     * @return
     */
    private BatchLendPayResp doLend(BatchLendPayReq batchLendPayReq, int retryNum) {
        if (retryNum <= 0) {
            log.error("安全放款严重BUG");
            exceptionEmailHelper.sendErrorMessage("安全放款失败", GSON.toJson(batchLendPayReq));
            return null;
        }

        BatchLendPayResp batchLendPayResp = jixinManager.send(JixinTxCodeEnum.BATCH_LEND_REPAY,
                batchLendPayReq,
                BatchLendPayResp.class);

        if ((!ObjectUtils.isEmpty(batchLendPayResp))
                && (!StringUtils.isEmpty(batchLendPayResp.getReceived()))
                && ("success".equalsIgnoreCase(batchLendPayResp.getReceived()))) {  // 成功
            return getSuccessBatchLendPayResp(batchLendPayReq);
        } else { // 失败
            if (JixinResultContants.isNetWordError(batchLendPayResp)) {  // 网络请求失败. 需要查询确认
                log.info("=========================================");
                log.info("批次放款出现网络问题, 系统进行容错处理");
                log.info("=========================================");
                BatchQueryResp batchQueryResp = queryBatchQuery(batchLendPayReq.getTxDate(), batchLendPayReq.getBatchNo(), 4);
                if (ObjectUtils.isEmpty(batchQueryResp)) {
                    log.error(String.format("批次查询严重BUG, 数据[%s]", GSON.toJson(batchLendPayReq)));
                    return null;
                }

                if (JixinResultContants.SUCCESS.equalsIgnoreCase(batchQueryResp.getRetCode())) {   // 查询成功
                    return getSuccessBatchLendPayResp(batchLendPayReq);
                } else {
                    return doLend(batchLendPayReq, retryNum - 1);
                }
            }

            if (JixinResultContants.isBusy(batchLendPayResp)) { //频率受限. 重新尝试即可
                log.info("=========================================");
                log.info("批次放款出现频率受限问题, 系统进行容错处理");
                log.info("=========================================");
                try {
                    Thread.sleep(1 * 1000);
                } catch (Exception e) {
                }

                return doLend(batchLendPayReq, retryNum - 1);
            }

            if ("JX900014".equalsIgnoreCase(batchLendPayResp.getRetCode())) { // 流水号重复, 重置请求, 重新发送
                log.info("=========================================");
                log.info("批次放款出现流水号重复问题, 系统进行容错处理");
                log.info("=========================================");
                batchLendPayReq.setTxTime(null);
                batchLendPayReq.setSeqNo(null);
                return doLend(batchLendPayReq, retryNum - 1);
            }

            return batchLendPayResp;
        }
    }

    private BatchLendPayResp getSuccessBatchLendPayResp(BatchLendPayReq batchLendPayReq) {
        BatchLendPayResp batchLendPayResp;
        batchLendPayResp = new BatchLendPayResp();
        batchLendPayResp.setRetCode(JixinResultContants.SUCCESS);
        batchLendPayResp.setRetMsg("成功");
        batchLendPayResp.setBatchNo(batchLendPayReq.getBatchNo());
        batchLendPayResp.setNotifyURL(batchLendPayReq.getNotifyURL());
        batchLendPayResp.setRetNotifyURL(batchLendPayReq.getRetNotifyURL());
        batchLendPayResp.setSubPacks(batchLendPayReq.getSubPacks());
        batchLendPayResp.setTxAmount(batchLendPayReq.getTxAmount());
        batchLendPayResp.setTxCounts(batchLendPayReq.getTxCounts());
        return batchLendPayResp;
    }


    /**
     * 安全取消放款
     *
     * @param batchLendPayReq
     */
    private void safeCancelLend(BatchLendPayReq batchLendPayReq) {
        log.info(String.format("安全撤销放款, 数据[%s]", GSON.toJson(batchLendPayReq)));
        // 先进行查询
        BatchQueryResp batchQueryResp = queryBatchQuery(batchLendPayReq.getTxDate(), batchLendPayReq.getBatchNo(), 4);
        if ((!ObjectUtils.isEmpty(batchQueryResp))
                && (JixinResultContants.SUCCESS.equalsIgnoreCase(batchQueryResp.getRetCode()))) { // 查询成功
            if ("A".equalsIgnoreCase(batchQueryResp.getBatchState())) {
                // 进行标的撤销
                BatchCancelReq batchCancelReq = new BatchCancelReq();
                batchCancelReq.setBatchNo(batchLendPayReq.getBatchNo());
                batchCancelReq.setTxAmount(batchLendPayReq.getTxAmount());
                batchCancelReq.setTxCounts(StringHelper.toString(batchLendPayReq.getTxCounts()));
                boolean b = doCancelBatch(batchCancelReq, 4);
                if (b) {
                    log.info("放款取消成功");
                } else {
                    log.error("放款取消失败");
                }

                exceptionEmailHelper.sendErrorMessage("取消放款",
                        String.format("数据:[%s], 状态: %s", GSON.toJson(batchLendPayReq), b ? "成功" : "失败"));

            } else if ("C".equalsIgnoreCase(batchQueryResp.getBatchState())) {
                log.info("=========================================");
                log.info(String.format("批次已经撤销, 请勿重复操作"));
                log.info("=========================================");
            } else {
                log.error(String.format("安全撤销放款: 当前批次不在待处理范围, 数据[%s]", GSON.toJson(batchQueryResp)));
            }
        } else {
            exceptionEmailHelper.sendErrorMessage("取消放款, 查询批次信息异常",
                    String.format("数据:[%s], 返回数据:[%s]", GSON.toJson(batchLendPayReq), GSON.toJson(batchQueryResp)));
        }
    }


    /**
     * 实际批次撤销
     *
     * @param batchCancelReq 批次类型
     * @param retryNum       重试次数
     * @return
     */
    private boolean doCancelBatch(BatchCancelReq batchCancelReq, int retryNum) {
        if (retryNum <= 0) {
            log.error(String.format("取消批次严重问题, 数据[%s]", GSON.toJson(batchCancelReq)));
            exceptionEmailHelper.sendErrorMessage("批次取消异常", GSON.toJson(batchCancelReq));
        }

        BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
        if (JixinResultContants.isNetWordError(batchCancelResp)) { // 网路问题
            BatchQueryResp batchQueryResp = queryBatchQuery(batchCancelReq.getTxDate(), batchCancelReq.getBatchNo(), 4);
            if ((!ObjectUtils.isEmpty(batchQueryResp))
                    && (JixinResultContants.SUCCESS.equalsIgnoreCase(batchQueryResp.getRetCode()))) {
                if ("C".equalsIgnoreCase(batchQueryResp.getBatchState())) {
                    return true;
                } else if ("A".equalsIgnoreCase(batchQueryResp.getBatchState())) {
                    return doCancelBatch(batchCancelReq, retryNum - 1);
                } else {
                    log.error("=================================");
                    log.error(String.format("批次为不可取消状态, 数据[%s]", GSON.toJson(batchCancelReq)));
                    log.error("=================================");
                    return false;
                }
            } else {
                log.error("出现严重撤销批次问题");
                return false;
            }
        }

        // 频率受限
        if (JixinResultContants.isBusy(batchCancelResp)) {
            log.info("=========================================");
            log.info("取消批次频率受限");
            log.info("=========================================");

            try {
                Thread.sleep(1 * 1000);
            } catch (Exception e) {
            }
            return doCancelBatch(batchCancelReq, retryNum - 1);
        }

        if ("JX900014".equalsIgnoreCase(batchCancelResp.getRetCode())) { // 流水号重复, 重置请求, 重新发送
            batchCancelReq.setTxTime(null);
            batchCancelReq.setSeqNo(null);
            log.info("=========================================");
            log.info("取消批次流水号重复问题, 系统进行容错处理");
            log.info("=========================================");
            return doCancelBatch(batchCancelReq, retryNum - 1);
        }

        if (JixinResultContants.SUCCESS.equalsIgnoreCase(batchCancelResp.getRetCode())) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 查询批次状态
     *
     * @param batchTxDate
     * @param batchNo
     * @param retryNum
     * @return
     */
    private BatchQueryResp queryBatchQuery(String batchTxDate, String batchNo, int retryNum) {
        log.info(String.format("即信批次查询, 批次:%s, 日期:%s", batchNo, batchTxDate));
        if (retryNum <= 0) {
            log.error(String.format("即信批次查询严重BUG, 批次:%s, 日期:%s", batchNo, batchTxDate));
            exceptionEmailHelper.sendErrorMessage("查询批次状态严重BUG", String.format("批次:%s, 日期:%s", batchNo, batchTxDate));
            return null;
        }

        BatchQueryReq batchQueryReq = new BatchQueryReq();
        batchQueryReq.setBatchNo(batchNo);
        batchQueryReq.setBatchTxDate(batchTxDate);
        BatchQueryResp batchQueryResp = jixinManager.send(JixinTxCodeEnum.BATCH_QUERY, batchQueryReq, BatchQueryResp.class);
        if (JixinResultContants.isNetWordError(batchQueryResp)) {  // 网络失败
            return queryBatchQuery(batchTxDate, batchNo, retryNum - 1);
        }

        if (JixinResultContants.isBusy(batchQueryResp)) {  // 网络频繁
            try {
                Thread.sleep(2 * 1000);
            } catch (Exception e) {
            }
            return queryBatchQuery(batchTxDate, batchNo, retryNum - 1);
        }

        return batchQueryResp;
    }

    /**
     * 标记新手投标标识
     *
     * @param borrow
     * @param tenderList
     */
    private void recordUserNoviceTender(Borrow borrow, List<Tender> tenderList) throws Exception {
        for (Tender tender : tenderList) {
            log.info(String.format("标记新手投标标识"));
            UserCache userCache = userCacheService.findById(tender.getUserId());

            if (borrow.isTransfer() && (!BooleanUtils.toBoolean(userCache.getTenderTransfer()))) {
                userCache.setTenderTransfer(borrow.getId().intValue());
                userCache.setTenderId(tender.getId());
            } else if ((borrow.getType() == 0) && (!BooleanUtils.toBoolean(userCache.getTenderTuijian()))) {
                userCache.setTenderTuijian(borrow.getId().intValue());
                userCache.setTenderId(tender.getId());
            } else if ((borrow.getType() == 1) && (!BooleanUtils.toBoolean(userCache.getTenderJingzhi()))) {
                userCache.setTenderJingzhi(borrow.getId().intValue());
                userCache.setTenderId(tender.getId());
            } else if ((borrow.getType() == 2) && (!BooleanUtils.toBoolean(userCache.getTenderMiao()))) {
                userCache.setTenderMiao(borrow.getId().intValue());
                userCache.setTenderId(tender.getId());
            } else if ((borrow.getType() == 4) && (!BooleanUtils.toBoolean(userCache.getTenderQudao()))) {
                userCache.setTenderQudao(borrow.getId().intValue());
                userCache.setTenderId(tender.getId());
            }

            userCacheService.save(userCache);
        }
    }

    /**
     * 新增借款放款资产变动
     *
     * @param nowDate
     * @param borrowId
     * @param borrow
     * @param sumNetWorthFee
     * @param batchNo
     * @throws ExecutionException
     */
    private void addBorrowLendRepayAssetChange(Date nowDate, Long borrowId, Borrow borrow, double sumNetWorthFee, String batchNo) throws ExecutionException {
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        long takeUserId = ObjectUtils.isEmpty(borrow.getTakeUserId()) ? borrow.getUserId() : borrow.getTakeUserId();
        // 获取待还
        long feeId = assetChangeProvider.getFeeAccountId();  // 收费账户
        //生成借款人资产变动记录
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setSourceId(borrowId);
        batchAssetChange.setState(0);
        batchAssetChange.setType(BatchAssetChangeContants.BATCH_LEND_REPAY);
        batchAssetChange.setCreatedAt(new Date());
        batchAssetChange.setUpdatedAt(new Date());
        batchAssetChange.setBatchNo(batchNo);
        batchAssetChangeService.save(batchAssetChange);
        long batchAssetChangeId = batchAssetChange.getId();
        // 借款人还款
        // 放款
        BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setSourceId(borrow.getId());
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItem.setMoney(borrow.getMoney());
        batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
        batchAssetChangeItem.setRemark(String.format("标的[%s]融资成功. 放款%s元", borrow.getName(), StringHelper.formatDouble(borrow.getMoney() / 100D, true)));
        batchAssetChangeItem.setType(AssetChangeTypeEnum.borrow.getLocalType());
        batchAssetChangeItem.setUserId(takeUserId);
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItemService.save(batchAssetChangeItem);

        // 信用账户管理费
        if (borrow.getType() == 1) {
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setSourceId(borrow.getId());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItem.setMoney(new Double(sumNetWorthFee).longValue());
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setRemark(String.format("扣除标的[%s]融资管理费%s元", borrow.getName(), StringHelper.formatDouble(MoneyHelper.divide(new Double(sumNetWorthFee).longValue(), 100D), true)));
            batchAssetChangeItem.setType(AssetChangeTypeEnum.financingManagementFee.getLocalType());
            batchAssetChangeItem.setUserId(takeUserId);
            batchAssetChangeItem.setForUserId(feeId);
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItemService.save(batchAssetChangeItem);  // 扣除融资管理费

            // 费用平台添加收取的转让费
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setSourceId(borrow.getId());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItem.setMoney(new Double(sumNetWorthFee).longValue());
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setRemark(String.format("收取标的[%s]融资管理费%s元", borrow.getName(), StringHelper.formatDouble(MoneyHelper.divide(new Double(sumNetWorthFee).longValue(), 100D), true)));
            batchAssetChangeItem.setType(AssetChangeTypeEnum.platformFinancingManagementFee.getLocalType());
            batchAssetChangeItem.setUserId(feeId);
            batchAssetChangeItem.setForUserId(takeUserId);
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItemService.save(batchAssetChangeItem);  // 收取融资管理费
        }
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<String> thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayCheckResp repayCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(repayCheckResp)) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(repayCheckResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        long repaymentId = NumberHelper.toLong(acqResMap.get("repaymentId"));
        if (!JixinResultContants.SUCCESS.equals(repayCheckResp.getRetCode())) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("回调失败! msg:" + repayCheckResp.getRetMsg());
            /* 还款会员id */
            long userId = NumberHelper.toLong(acqResMap.get("userId"));
            UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(userId);
            String freezeOrderId = StringHelper.toString(acqResMap.get("freezeOrderId"));
            String freezeMoney = StringHelper.formatDouble(MoneyHelper.round(NumberHelper.toDouble(acqResMap.get("freezeMoney")), 2), false);//元

            //解除存管资金冻结
            String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);
            BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
            balanceUnfreezeReq.setAccountId(borrowUserThirdAccount.getAccountId());
            balanceUnfreezeReq.setTxAmount(freezeMoney);
            balanceUnfreezeReq.setChannel(ChannelContant.HTML);
            balanceUnfreezeReq.setOrderId(orderId);
            balanceUnfreezeReq.setOrgOrderId(freezeOrderId);
            BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
            if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
                log.error("===========================================================================");
                log.error("即信批次还款解除冻结资金失败：" + balanceUnfreezeResp.getRetMsg());
                log.error("===========================================================================");
                return ResponseEntity.ok("error");
            }
            //立即还款冻结
            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.unfreeze);  // 招标失败解除冻结资金
            assetChange.setUserId(userId);
            assetChange.setMoney(new Double(NumberHelper.toDouble(freezeMoney) * 100).longValue());
            assetChange.setRemark("即信批次还款解除冻结可用资金");
            assetChange.setSourceId(repaymentId);
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error("即信批次还款解除冻结可用资金异常:", e);
            }

            //更新即信放款状态 为处理失败!
            BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);
            borrowRepayment.setRepayStatus(ThirdDealStatusContrants.INDISPOSE);
            borrowRepayment.setUpdatedAt(new Date());
            borrowRepaymentService.save(borrowRepayment);

            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), repaymentId, 2, ThirdBatchLogContants.BATCH_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(repayCheckResp.getBatchNo(), repaymentId,
                    ThirdBatchDealLogContants.PARAM_CHECK, false, ThirdBatchLogContants.BATCH_REPAY, "");
        } else {
            log.info("=============================即信批次还款检验参数回调===========================");
            log.info("回调成功!");
            try {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), repaymentId, 1, ThirdBatchLogContants.BATCH_REPAY);
                //记录批次处理日志
                thirdBatchDealLogBiz.recordThirdBatchDealLog(repayCheckResp.getBatchNo(), repaymentId,
                        ThirdBatchDealLogContants.PARAM_CHECK, true, ThirdBatchLogContants.BATCH_REPAY, GSON.toJson(repayCheckResp));
            } catch (Exception e) {
                log.error("更新批次日志记录失败:", e);
            }
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    @Override
    public ResponseEntity<String> thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        log.info("即信批次回调触发");
        BatchRepayRunResp repayRunResp = jixinManager.callback(request, new TypeToken<BatchRepayRunResp>() {
        });
        return dealBatchRepay(repayRunResp);
    }

    /**
     * 处理即信批次还款
     *
     * @param repayRunResp
     * @return
     */
    @Override
    public ResponseEntity<String> dealBatchRepay(BatchRepayRunResp repayRunResp) {
        Preconditions.checkNotNull(repayRunResp, "即信批次回调触发: 请求体为空!");
        Preconditions.checkState(JixinResultContants.SUCCESS.equals(repayRunResp.getRetCode()), String.format("即信批次回调触发: 验证失败 %s", repayRunResp.getRetMsg()));

        Map<String, Object> acqResMap = GSON.fromJson(repayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        //触发处理批次放款处理结果队列
        //推送批次处理到队列中
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = new ImmutableMap.Builder<String, String>()
                .put(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("repaymentId")))
                .put(MqConfig.BATCH_NO, repayRunResp.getBatchNo())
                .put(MqConfig.BATCH_TYPE, String.valueOf(ThirdBatchLogContants.BATCH_REPAY))
                .put(MqConfig.MSG_TIME, DateHelper.dateToString(new Date()))
                .put(MqConfig.ACQ_RES, repayRunResp.getAcqRes())
                .put(MqConfig.BATCH_RESP, GSON.toJson(repayRunResp))
                .build();

        mqConfig.setMsg(body);
        try {
            log.info(String.format("BorrowRepaymentThirdBizImpl dealBatchRepay send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("BorrowRepaymentThirdBizImpl dealBatchRepay send mq exception", e);
        }
        log.info("即信批次回调处理结束");
        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    @Override
    public ResponseEntity<String> thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchLendPayCheckResp lendRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchLendPayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(lendRepayCheckResp)) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(lendRepayCheckResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long borrowId = NumberHelper.toLong(acqResMap.get("borrowId"));
        if (!JixinResultContants.SUCCESS.equals(lendRepayCheckResp.getRetCode())) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("回调失败! msg:" + lendRepayCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), borrowId, 2, ThirdBatchLogContants.BATCH_LEND_REPAY);
            //改变批次放款状态 处理失败
            Borrow borrow = borrowService.findById(borrowId);
            borrow.setLendRepayStatus(ThirdDealStatusContrants.INDISPOSE);
            borrowService.save(borrow);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(lendRepayCheckResp.getBatchNo(), borrowId, ThirdBatchDealLogContants.PARAM_CHECK, false,
                    ThirdBatchLogContants.BATCH_LEND_REPAY, lendRepayCheckResp.getRetMsg());
        } else {
            log.info("=============================即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), borrowId, 1, ThirdBatchLogContants.BATCH_LEND_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(lendRepayCheckResp.getBatchNo(), borrowId, ThirdBatchDealLogContants.PARAM_CHECK, true,
                    ThirdBatchLogContants.BATCH_LEND_REPAY, lendRepayCheckResp.getRetMsg());
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
        return dealBatchLendRepay(lendRepayRunResp);
    }

    /**
     * 处理即信批次放款
     *
     * @param lendRepayRunResp
     * @return
     */
    @Override
    public ResponseEntity<String> dealBatchLendRepay(BatchLendPayRunResp lendRepayRunResp) {
        log.info("进入批次放款处理流程!");
        Preconditions.checkNotNull(lendRepayRunResp, "即信请求体为空！");
        log.info("即信请求体:", GSON.toJson(lendRepayRunResp));
        Preconditions.checkState(JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode()), "即信回调反馈：处理失败! msg:" + lendRepayRunResp.getRetMsg());
        Map<String, Object> acqResMap = GSON.fromJson(lendRepayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);


        //触发处理批次放款处理结果队列
        //推送批次处理到队列中
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = new ImmutableMap.Builder<String, String>()
                .put(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("borrowId")))
                .put(MqConfig.BATCH_NO, lendRepayRunResp.getBatchNo())
                .put(MqConfig.BATCH_TYPE, String.valueOf(ThirdBatchLogContants.BATCH_LEND_REPAY))
                .put(MqConfig.MSG_TIME, DateHelper.dateToString(new Date()))
                .put(MqConfig.ACQ_RES, lendRepayRunResp.getAcqRes())
                .put(MqConfig.BATCH_RESP, GSON.toJson(lendRepayRunResp))
                .build();

        mqConfig.setMsg(body);
        try {
            log.info(String.format("BorrowRepaymentThirdBizImpl dealBatchLendRepay send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("BorrowRepaymentThirdBizImpl dealBatchLendRepay send mq exception", e);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 批次名义借款人垫付参数检查回调
     */
    @Override
    public ResponseEntity<String> thirdBatchAdvanceCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayCheckResp batchBailRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchBailRepayCheckResp>() {
        });
        if (ObjectUtils.isEmpty(batchBailRepayCheckResp)) {
            log.error("=============================批次名义借款人垫付参数检查回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(batchBailRepayCheckResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long repaymentId = NumberHelper.toLong(acqResMap.get("repaymentId"));
        if (!JixinResultContants.SUCCESS.equals(batchBailRepayCheckResp.getRetCode())) {
            log.error("=============================批次名义借款人垫付参数检查回调===========================");
            log.error("回调失败! msg:" + batchBailRepayCheckResp.getRetMsg());
            BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);
            borrowRepayment.setIsAdvance(false);
            borrowRepayment.setUpdatedAt(new Date());
            borrowRepaymentService.save(borrowRepayment);

            //垫付失败解冻账户资金
            if (unfreezeAssetByAdvance(batchBailRepayCheckResp, acqResMap, repaymentId)) {
                return ResponseEntity.ok("error");
            }
            //取消垫付债权转让
            cancelAdvanceTransfer(borrowRepayment);

            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), repaymentId, 2, ThirdBatchLogContants.BATCH_BAIL_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(batchBailRepayCheckResp.getBatchNo(), repaymentId,
                    ThirdBatchDealLogContants.PARAM_CHECK, false, ThirdBatchLogContants.BATCH_BAIL_REPAY, GSON.toJson(batchBailRepayCheckResp));
        } else {
            log.info("=============================批次名义借款人垫付参数成功回调===========================");
            log.info("回调成功!");
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), repaymentId, 1, ThirdBatchLogContants.BATCH_BAIL_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(batchBailRepayCheckResp.getBatchNo(), repaymentId,
                    ThirdBatchDealLogContants.PARAM_CHECK, true, ThirdBatchLogContants.BATCH_BAIL_REPAY, "");
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 取消垫付债权转让
     *
     * @param borrowRepayment
     */
    private void cancelAdvanceTransfer(BorrowRepayment borrowRepayment) {
        /* 查询投资列表 */
        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowRepayment.getBorrowId())
                .build();
        List<Tender> tenderList = tenderService.findList(specification);
        Preconditions.checkState(!CollectionUtils.isEmpty(tenderList), "投资人投标信息不存在!");
        /* 投资记录id集合 */
        List<Long> tenderIds = tenderList.stream().map(tender -> tender.getId()).collect(Collectors.toList());
        Map<Long, Tender> tenderMaps = tenderList.stream().collect(Collectors.toMap(Tender::getId, Function.identity()));
        /* 查询未转让的回款记录 */
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("order", borrowRepayment.getOrder())
                .eq("transferFlag", 0)
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Preconditions.checkState(!CollectionUtils.isEmpty(borrowCollectionList), "投资回款记录不存在!");
        //查询债权转让记录
        Specification<Transfer> transferSpecification = Specifications
                .<Transfer>and()
                .in("tenderId", tenderIds.toArray())
                .build();
        List<Transfer> transferList = transferService.findList(transferSpecification);
        Preconditions.checkState(!CollectionUtils.isEmpty(transferList), "债权转让记录不存在!");
        List<Long> transferIds = transferList.stream().map(Transfer::getId).collect(Collectors.toList());
        Map<Long/* 投标id */, Transfer> transferMaps = transferList.stream().collect(Collectors.toMap(Transfer::getTenderId, Function.identity()));
        /* 购买债权转让记录 */
        Specification<TransferBuyLog> tbls = Specifications
                .<TransferBuyLog>and()
                .in("transferId", transferIds.toArray())
                .build();
        List<TransferBuyLog> transferBuyLogList = transferBuyLogService.findList(tbls);
        Preconditions.checkState(!CollectionUtils.isEmpty(transferBuyLogList), "购买债权转让记录不能为空!");
        //
        borrowCollectionList.stream().forEach(borrowCollection -> {
            //更新tender状态
            Tender tender = tenderMaps.get(borrowCollection.getTenderId());
            tender.setTransferFlag(0);

            tender.setUpdatedAt(new Date());
            //取消债权转让
            Transfer transfer = transferMaps.get(tender.getId());
            VoEndTransfer voEndTransfer = new VoEndTransfer();
            voEndTransfer.setUserId(transfer.getUserId());
            voEndTransfer.setTransferId(transfer.getId());
            try {
                transferBiz.endTransfer(voEndTransfer);
            } catch (Exception e) {
                log.error("borrowRepaymentThirdBiz thirdBatchAdvanceCheckCall error:", e);
            }
        });
        tenderService.save(tenderList);
    }

    /**
     * 垫付失败解冻账户资金
     *
     * @param batchBailRepayCheckResp
     * @param acqResMap
     * @param repaymentId
     * @return
     */
    private boolean unfreezeAssetByAdvance(BatchBailRepayCheckResp batchBailRepayCheckResp, Map<String, Object> acqResMap, Long repaymentId) {
        String accountId = StringHelper.toString(acqResMap.get("accountId"));//担保人账户id
        String txAmount = batchBailRepayCheckResp.getTxAmount();
        UserThirdAccount titularUserThirdAccount = userThirdAccountService.findByAccountId(accountId);//担保人存管信息

        //解除本地冻结
        //立即还款冻结
        long frozenMoney = new Double(MoneyHelper.multiply(NumberHelper.toDouble(txAmount), 100)).longValue();
        AssetChange assetChange = new AssetChange();
        assetChange.setType(AssetChangeTypeEnum.unfreeze);  // 招标失败解除冻结资金
        assetChange.setUserId(titularUserThirdAccount.getUserId());
        assetChange.setMoney(frozenMoney);
        assetChange.setRemark("名义借款人垫付解除冻结可用资金");
        assetChange.setSourceId(repaymentId);
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error("名义借款人垫付解除冻结可用资金异常:", e);
        }
        return false;
    }

    /**
     * 批次名义借款人垫付业务处理回调
     */
    @Override
    public ResponseEntity<String> thirdBatchAdvanceRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayRunResp batchBailRepayRunResp = jixinManager.callback(request, new TypeToken<BatchBailRepayRunResp>() {
        });
        return dealBatchAdvance(batchBailRepayRunResp);
    }

    /**
     * 处理批次名义借款人垫付处理
     *
     * @param batchBailRepayRunResp
     * @return
     */
    @Override
    public ResponseEntity<String> dealBatchAdvance(BatchBailRepayRunResp batchBailRepayRunResp) {

        log.info("进入批次名义借款人垫付业务处理流程");
        log.info("请求参数：" + GSON.toJson(batchBailRepayRunResp));
        Preconditions.checkNotNull(batchBailRepayRunResp, "批次名义借款人垫付业务处理回调，请求体为空!");
        Preconditions.checkState(JixinResultContants.SUCCESS.equals(batchBailRepayRunResp.getRetCode()), "批次名义借款人垫付业务处理回调：回调失败！ msg:" + batchBailRepayRunResp.getRetMsg());
        Map<String, Object> acqResMap = GSON.fromJson(batchBailRepayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        //=============================================
        // 保存批次名义借款人垫付授权号
        //=============================================
        try {
            List<CreditInvestRun> creditInvestRunList = GSON.fromJson(batchBailRepayRunResp.getSubPacks(), new TypeToken<List<CreditInvestRun>>() {
            }.getType());
            saveThirdTransferAuthCode(creditInvestRunList);
        } catch (JsonSyntaxException e) {
            log.error("保存批次名义借款人垫付授权号!", e);
        }

        //触发批次名义借款人垫付业务处理队列
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = new ImmutableMap.Builder<String, String>()
                .put(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("borrowId")))
                .put(MqConfig.BATCH_NO, batchBailRepayRunResp.getBatchNo())
                .put(MqConfig.BATCH_TYPE, String.valueOf(ThirdBatchLogContants.BATCH_BAIL_REPAY))
                .put(MqConfig.MSG_TIME, DateHelper.dateToString(new Date()))
                .put(MqConfig.ACQ_RES, batchBailRepayRunResp.getAcqRes())
                .put(MqConfig.BATCH_RESP, GSON.toJson(batchBailRepayRunResp))
                .build();

        mqConfig.setMsg(body);
        try {
            log.info(String.format("BorrowRepaymentThirdBizImpl dealBatchAdvance send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("BorrowRepaymentThirdBizImpl dealBatchAdvance send mq exception", e);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 保存批次名义借款人授权号
     *
     * @param creditInvests
     */
    public void saveThirdTransferAuthCode(List<CreditInvestRun> creditInvests) {
        List<String> orderList = creditInvests.stream().map(creditInvest -> creditInvest.getOrderId()).collect(Collectors.toList());
        Specification<TransferBuyLog> bcs = Specifications
                .<TransferBuyLog>and()
                .eq("thirdTransferOrderId", orderList.toArray())
                .build();
        int pageIndex = 0;
        int maxPageSize = 50;
        Pageable pageable = null;
        List<TransferBuyLog> transferBuyLogList = null;
        do {
            pageable = new PageRequest(pageIndex++, maxPageSize, new Sort(Sort.Direction.ASC, "id"));
            transferBuyLogList = transferBuyLogService.findList(bcs, pageable);
            if (CollectionUtils.isEmpty(transferBuyLogList)) {
                break;
            }

            Map<String, TransferBuyLog> transferBuyLogMaps = transferBuyLogList.stream().collect(Collectors.toMap(TransferBuyLog::getThirdTransferOrderId, Function.identity()));
            creditInvests.stream().forEach(creditInvest -> {
                TransferBuyLog transferBuyLog = transferBuyLogMaps.get(creditInvest.getOrderId());
                transferBuyLog.setTransferAuthCode(creditInvest.getAuthCode());
            });
            transferBuyLogService.save(transferBuyLogList);
        } while (transferBuyLogList.size() >= maxPageSize);
    }

}
