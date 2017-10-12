package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestCheckCall;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestRunCall;
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvestRun;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryRequest;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResponse;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyRequest;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyResponse;
import com.gofobao.framework.api.model.bid_cancel.BidCancelReq;
import com.gofobao.framework.api.model.bid_cancel.BidCancelResp;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/6/1.
 */
@Service
@Slf4j
public class TenderThirdBizImpl implements TenderThirdBiz {

    final Gson GSON = new GsonBuilder().create();

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Autowired
    private JixinManager jixinManager;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;

    @Autowired
    private TransferBuyLogService transferBuyLogService;

    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    JixinHelper jixinHelper;

    public ResponseEntity<VoBaseResp> createThirdTender(VoCreateThirdTenderReq voCreateThirdTenderReq) {
        Long userId = voCreateThirdTenderReq.getUserId();
        String txAmount = voCreateThirdTenderReq.getTxAmount();
        String orderId = voCreateThirdTenderReq.getOrderId();
        Preconditions.checkNotNull(orderId, "即信投标申请orderId不能为空!");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "投标人未开户!");

        String autoTenderOrderId = userThirdAccount.getAutoTenderOrderId();
        if (StringUtils.isEmpty(autoTenderOrderId)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标人未进行投标签约!"));
        }
        Long autoTenderTxAmount = userThirdAccount.getAutoTenderTxAmount();
        if (autoTenderTxAmount < MoneyHelper.yuanToFen(txAmount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标金额超出签约单笔最大投标额!"));
        }
        Long autoTenderTotAmount = userThirdAccount.getAutoTenderTotAmount();
        if (autoTenderTotAmount < MoneyHelper.yuanToFen(txAmount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标金额超出签约总投标额!"));
        }

        BidAutoApplyRequest bidAutoApplyRequest = new BidAutoApplyRequest();
        bidAutoApplyRequest.setAccountId(userThirdAccount.getAccountId());
        bidAutoApplyRequest.setOrderId(orderId);
        bidAutoApplyRequest.setTxAmount(voCreateThirdTenderReq.getTxAmount());
        bidAutoApplyRequest.setProductId(voCreateThirdTenderReq.getProductId());
        bidAutoApplyRequest.setFrzFlag(voCreateThirdTenderReq.getFrzFlag());
        bidAutoApplyRequest.setContOrderId(autoTenderOrderId);
        bidAutoApplyRequest.setAcqRes(voCreateThirdTenderReq.getAcqRes());
        bidAutoApplyRequest.setChannel(ChannelContant.HTML);
        BidAutoApplyResponse bidAutoApplyResponse = doSafetyRegisterJixinTender(bidAutoApplyRequest, 3);  // 安全自动投标申报, 会有重试机制
        if ((ObjectUtils.isEmpty(bidAutoApplyResponse))
                || (!JixinResultContants.SUCCESS.equals(bidAutoApplyResponse.getRetCode()))) {
            // 此处为了安全起见, 对于即信投标失败, 主动进行撤回, 并且把投标记录设置为投标失败
            Tender updTender = tenderService.findById(NumberHelper.toLong(bidAutoApplyRequest.getAcqRes()));
            updTender.setTUserId(userThirdAccount.getId());
            updTender.setThirdTenderOrderId(orderId);
            updTender.setStatus(0);  // 投标失败
            updTender.setUpdatedAt(new Date()); // 更新投标时间
            tenderService.updateById(updTender);
            cancelJixinTenderRecord(bidAutoApplyRequest, 4); // 取消即信投标
            String msg = ObjectUtils.isEmpty(bidAutoApplyResponse) ? "当前网络不稳定，请稍候重试" : bidAutoApplyResponse.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        } else {  // 更新投标记录
            Tender updTender = tenderService.findById(NumberHelper.toLong(bidAutoApplyRequest.getAcqRes()));
            updTender.setAuthCode(bidAutoApplyResponse.getAuthCode());
            updTender.setTUserId(userThirdAccount.getId());
            updTender.setThirdTenderOrderId(orderId);
            tenderService.updateById(updTender);
            return ResponseEntity.ok(VoBaseResp.ok("创建投标成功!"));
        }
    }

    /**
     * 取消即信自动投标申请
     *
     * @param bidAutoApplyRequest
     * @param retryNum            重试次数
     * @return
     */
    public boolean cancelJixinTenderRecord(BidAutoApplyRequest bidAutoApplyRequest, int retryNum) {
        String data = GSON.toJson(bidAutoApplyRequest);
        if (retryNum <= 0) {
            return false;
        }
        BidCancelReq bidCancelReq = new BidCancelReq();
        String orderId = JixinHelper.getOrderId(JixinHelper.CANCEL_TENDER_PREFIX);
        bidCancelReq.setAccountId(bidAutoApplyRequest.getAccountId()); // 电子账户ID
        bidCancelReq.setOrderId(orderId);  // 取消投标申请
        bidCancelReq.setOrgOrderId(bidAutoApplyRequest.getOrderId()); // 原始投标Id
        bidCancelReq.setProductId(bidAutoApplyRequest.getProductId()); // 投标ID
        bidCancelReq.setTxAmount(bidAutoApplyRequest.getTxAmount()); // 投标金额
        BidCancelResp bidCancelResp = jixinManager.send(JixinTxCodeEnum.BID_CANCEL, bidCancelReq, BidCancelResp.class);
        if (ObjectUtils.isEmpty(bidCancelResp)) {
            log.error(String.format("取消即信自动投标申请失败, 原因即信响应体为空. 数据[%s]", data));
            return cancelJixinTenderRecord(bidAutoApplyRequest, retryNum - 1);
        }


        if (JixinResultContants.SUCCESS.equals(bidCancelResp.getRetCode())) {
            log.info("取消即信自动投标申请成功");
            exceptionEmailHelper.sendErrorMessage("自动投标申请取消成功", data);
            return true;
        } else {
            if ((JixinResultContants.ERROR_502.equalsIgnoreCase(bidCancelResp.getRetCode()))  // 502
                    || (JixinResultContants.ERROR_504.equalsIgnoreCase(bidCancelResp.getRetCode())) // 504
                    || (JixinResultContants.ERROR_JX900032.equalsIgnoreCase(bidCancelResp.getRetCode()))) {  // 出现超时
                try {
                    Thread.sleep(3 * 1000);
                } catch (Exception e) {
                }
                return cancelJixinTenderRecord(bidAutoApplyRequest, retryNum - 1);
            } else {
                String msg = String.format("取消即信自动投标申请失败, 原因: %s, 数据: %s", bidCancelResp.getRetMsg(), data);
                log.error(msg);
                exceptionEmailHelper.sendErrorMessage("自动投标申请取消失败", msg);
                return false;
            }
        }
    }

    /**
     * 理财计划批次购买债权参数验证回调
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchCreditInvestFinanceCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchCreditInvestCheckCall batchCreditInvestCheckCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestCheckCall>() {
        });

        if (ObjectUtils.isEmpty(batchCreditInvestCheckCall)) {
            log.error("=============================理财计划批次购买债权参数验证回调===========================");
            log.error("请求体为空!");
            ResponseEntity.ok("error");
        }

        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestCheckCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long transferId = NumberHelper.toLong(acqResMap.get("transferId"));
        if (!JixinResultContants.SUCCESS.equals(batchCreditInvestCheckCall.getRetCode())) {
            log.error("=============================理财计划批次购买债权参数验证回调===========================");
            log.error("回调失败! msg:" + batchCreditInvestCheckCall.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestCheckCall.getBatchNo(), transferId, 2, ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST);
            ResponseEntity.ok("error");
        } else {
            log.error("=============================理财计划批次购买债权参数验证回调===========================");
            log.error("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestCheckCall.getBatchNo(), transferId, 1, ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 理财计划批次购买债权参数运行回调
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchCreditInvestFinanceRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchCreditInvestRunCall batchCreditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });
        return dealFinanceBatchCreditInvest(batchCreditInvestRunCall);
    }

    /**
     * 处理理财计划批次购买债权转让
     *
     * @param batchCreditInvestRunCall
     * @return
     */
    public ResponseEntity<String> dealFinanceBatchCreditInvest(BatchCreditInvestRunCall batchCreditInvestRunCall) {
        Gson gson = new Gson();
        log.info(String.format("理财计划批量债权购买回调信息打印: %s", gson.toJson(batchCreditInvestRunCall)));
        Preconditions.checkNotNull(batchCreditInvestRunCall, "理财计划批量债权转让回调: 回调信息为空!");
        Preconditions.checkArgument(JixinResultContants.SUCCESS.equals(batchCreditInvestRunCall.getRetCode()),
                "理财计划批量债权转让回调: 请求失败!");
        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestRunCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        //=============================================
        // 保存第三方债权转让授权码
        //=============================================
        try {
            List<CreditInvestRun> creditInvestRunList = GSON.fromJson(batchCreditInvestRunCall.getSubPacks(), new TypeToken<List<CreditInvestRun>>() {
            }.getType());
            Preconditions.checkNotNull(creditInvestRunList, "理财计划批量债权转让回调: 查询批次详情为空");
            saveThirdTransferAuthCode(creditInvestRunList);
        } catch (JsonSyntaxException e) {
            log.error("理财计划批量债权转让保存第三方债权转让授权码!", e);
        }

        // 触发处理批次购买债权处理队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("transferId")), batchCreditInvestRunCall.getBatchNo(),
                    ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST, batchCreditInvestRunCall.getAcqRes(), GSON.toJson(batchCreditInvestRunCall));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }
        return ResponseEntity.ok("success");
    }


    /**
     * 安全申报投标
     *
     * @param bidAutoApplyRequest
     * @param retryNum
     * @return
     */
    private BidAutoApplyResponse doSafetyRegisterJixinTender(BidAutoApplyRequest bidAutoApplyRequest, int retryNum) {
        String data = "";
        if (retryNum <= 0) {
            exceptionEmailHelper.sendErrorMessage("安全申报自动投标严重BUG", String.format("数据[%s]", data));
            return null;
        }

        try {
            BidAutoApplyResponse bidAutoApplyResponse = jixinManager.send(JixinTxCodeEnum.BID_AUTO_APPLY,
                    bidAutoApplyRequest,
                    BidAutoApplyResponse.class);
            data = GSON.toJson(bidAutoApplyRequest);
            log.info("======================================");
            log.info(String.format("即信自动投标申请: %s", data));
            log.info("======================================");
            if ((ObjectUtils.isEmpty(bidAutoApplyResponse))) {
                log.error(String.format("安全申报自动投标失败[%s], 响应体为NULL", data));
                exceptionEmailHelper.sendErrorMessage("安全申报自动投标严重BUG", String.format("安全申报自动投标失败[%s], 响应体为NULL", data));
                return doSafetyRegisterJixinTender(bidAutoApplyRequest, retryNum - 1);
            }

            if (!JixinResultContants.SUCCESS.equalsIgnoreCase(bidAutoApplyResponse.getRetCode())) {
                log.error(String.format("安全申报自动投标失败[%s], 原因:%s", data, bidAutoApplyResponse.getRetMsg()));
                // 创建投标超时类型处理
                if ((JixinResultContants.ERROR_502.equalsIgnoreCase(bidAutoApplyResponse.getRetCode()))  // 502
                        || (JixinResultContants.ERROR_504.equalsIgnoreCase(bidAutoApplyResponse.getRetCode())) // 504
                        || (JixinResultContants.ERROR_WAIT_CT9903.equalsIgnoreCase(bidAutoApplyResponse.getRetCode()))  // 出现超时
                        || ("JX900044".equalsIgnoreCase(bidAutoApplyResponse.getRetCode()))) {  // 订单号已存在

                    try {
                        Thread.sleep(2 * 1000);
                    } catch (Exception e) {
                    }

                    BidApplyQueryResponse bidApplyQueryResponse = queryJixinTenderRecord(bidAutoApplyRequest.getAccountId(),
                            bidAutoApplyRequest.getOrderId(),
                            4);  // 即信交易查询

                    if ((!ObjectUtils.isEmpty(bidApplyQueryResponse))
                            && (bidApplyQueryResponse.getRetCode().equalsIgnoreCase(JixinResultContants.SUCCESS))) {  // 查询成功
                        bidAutoApplyResponse = new BidAutoApplyResponse();
                        bidAutoApplyResponse.setAccountId(bidAutoApplyRequest.getAccountId());
                        bidAutoApplyResponse.setAuthCode(bidApplyQueryResponse.getAuthCode());
                        bidAutoApplyResponse.setOrderId(bidAutoApplyRequest.getOrderId());
                        bidAutoApplyResponse.setName(bidApplyQueryResponse.getName());
                        bidAutoApplyResponse.setProductId(bidApplyQueryResponse.getProductId());
                        bidAutoApplyResponse.setTxAmount(bidApplyQueryResponse.getTxAmount());
                        bidAutoApplyResponse.setRetCode(JixinResultContants.SUCCESS);
                        return bidAutoApplyResponse;
                    } else { // 查询失败, 重新创建投标
                        log.error("安全申报自动投标失败: 查询记录失败, 系统进行尝试");
                        if (!ObjectUtils.isEmpty(bidAutoApplyResponse)) {
                            // 对于JX900044(orderId重复现象). 如果查询账户确实没有该投标记录, 说明order生成存在重复现象. 直接返回null, 不必继续重试
                            if ("JX900044".equalsIgnoreCase(bidAutoApplyResponse.getRetCode())) {
                                log.error(String.format("安全申报自动投标失败[%s], orderId 不唯一", data));
                                return null;
                            }
                        }
                        return doSafetyRegisterJixinTender(bidAutoApplyRequest, retryNum - 1);
                    }
                }

                // 访问频率超限
                if (JixinResultContants.ERROR_JX900032.equalsIgnoreCase(bidAutoApplyResponse.getRetCode())) {
                    log.error("安全申报自动投标失败: 频率受限, 系统进行尝试");
                    try {
                        Thread.sleep(1 * 1000);
                    } catch (Exception e) {
                    }
                    return doSafetyRegisterJixinTender(bidAutoApplyRequest, retryNum - 1);
                }

                return null;
            } else { // 查询成功
                return bidAutoApplyResponse;
            }

        } catch (Exception e) {
            log.error(String.format("安全申报自动投标失败[%s]", data), e);
            return null;
        }
    }


    /**
     * 查询即信投标记录
     *
     * @param accountId  电子账号
     * @param orgOrderId 原始投标OrderId
     * @param retryNum   尝试次数
     * @return
     */
    private BidApplyQueryResponse queryJixinTenderRecord(String accountId, String orgOrderId, long retryNum) {
        if (retryNum <= 0) {
            exceptionEmailHelper.sendErrorMessage("查询即信投标记录严重BUG", String.format("数据[ 账号:  %s, OrderId: %s]", accountId, orgOrderId));
            return null;
        }

        String data = "";
        BidApplyQueryRequest bidApplyQueryRequest = new BidApplyQueryRequest();
        bidApplyQueryRequest.setAccountId(accountId);
        bidApplyQueryRequest.setOrgOrderId(orgOrderId);
        BidApplyQueryResponse bidApplyQueryResponse = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY,
                bidApplyQueryRequest,
                BidApplyQueryResponse.class);
        data = GSON.toJson(bidApplyQueryRequest);  // 查询即信投标记录
        log.info("======================================");
        log.info(String.format("查询即信投标记录: %s", data));
        log.info("======================================");

        // 是否存在超时, 超时再次查询 || 或者查询为空
        if ((ObjectUtils.isEmpty(bidApplyQueryResponse))  // 查询对象为空
                || (JixinResultContants.ERROR_502.equalsIgnoreCase(bidApplyQueryResponse.getRetCode()))  // 502
                || (JixinResultContants.ERROR_504.equalsIgnoreCase(bidApplyQueryResponse.getRetCode()))  // 504
                || (JixinResultContants.ERROR_WAIT_CT9903.equalsIgnoreCase(bidApplyQueryResponse.getRetCode())) // 交易超时
                || (JixinResultContants.ERROR_JX900032.equalsIgnoreCase(bidApplyQueryResponse.getRetCode()))) { //访问频率超限
            try {
                Thread.sleep(3 * 1000);
            } catch (Exception e) {
            }

            log.error(String.format("查询即信投标记录失败[%s], 原因可能是超时系统拒绝访问", data));
            return queryJixinTenderRecord(accountId, orgOrderId, retryNum - 1);
        }

        if (JixinResultContants.SUCCESS.equalsIgnoreCase(bidApplyQueryResponse.getRetCode())) {
            return bidApplyQueryResponse;
        } else {
            return bidApplyQueryResponse;
        }
    }

    /**
     * 投资人批次购买债权参数验证回调
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchCreditInvestCheckCall(HttpServletRequest request, HttpServletResponse
            response) {
        BatchCreditInvestCheckCall batchCreditInvestCheckCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestCheckCall>() {
        });

        if (ObjectUtils.isEmpty(batchCreditInvestCheckCall)) {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("请求体为空!");
            ResponseEntity.ok("error");
        }

        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestCheckCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long transferId = NumberHelper.toLong(acqResMap.get("transferId"));
        if (!JixinResultContants.SUCCESS.equals(batchCreditInvestCheckCall.getRetCode())) {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("回调失败! msg:" + batchCreditInvestCheckCall.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestCheckCall.getBatchNo(), transferId, 2, ThirdBatchLogContants.BATCH_CREDIT_INVEST);
            ResponseEntity.ok("error");
        } else {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestCheckCall.getBatchNo(), transferId, 1, ThirdBatchLogContants.BATCH_CREDIT_INVEST);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 投资人批次购买债权运行回调
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse
            response) throws Exception {
        BatchCreditInvestRunCall batchCreditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });
        return dealBatchCreditInvest(batchCreditInvestRunCall);
    }

    /**
     * 处理批次购买债权转让
     *
     * @param batchCreditInvestRunCall
     * @return
     */
    public ResponseEntity<String> dealBatchCreditInvest(BatchCreditInvestRunCall batchCreditInvestRunCall) {
        Gson gson = new Gson();
        log.info(String.format("批量债权购买回调信息打印: %s", gson.toJson(batchCreditInvestRunCall)));
        Preconditions.checkNotNull(batchCreditInvestRunCall, "批量债权转让回调: 回调信息为空!");
        Preconditions.checkArgument(JixinResultContants.SUCCESS.equals(batchCreditInvestRunCall.getRetCode()),
                "批量债权转让回调: 请求失败!");
        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestRunCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        //=============================================
        // 保存第三方债权转让授权码
        //=============================================
        try {
            List<CreditInvestRun> creditInvestRunList = GSON.fromJson(batchCreditInvestRunCall.getSubPacks(), new TypeToken<List<CreditInvestRun>>() {
            }.getType());
            Preconditions.checkNotNull(creditInvestRunList, "批量债权转让回调: 查询批次详情为空");
            saveThirdTransferAuthCode(creditInvestRunList);
        } catch (JsonSyntaxException e) {
            log.error("保存第三方债权转让授权码!", e);
        }

        // 触发处理批次购买债权处理队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("transferId")), batchCreditInvestRunCall.getBatchNo(),
                    ThirdBatchLogContants.BATCH_CREDIT_INVEST, batchCreditInvestRunCall.getAcqRes(), GSON.toJson(batchCreditInvestRunCall));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }
        return ResponseEntity.ok("success");
    }

    /**
     * 保存第三债权转让授权号
     *
     * @param creditInvestRunList
     */
    public void saveThirdTransferAuthCode(List<CreditInvestRun> creditInvestRunList) {
        List<String> orderIds = creditInvestRunList.stream().map(creditInvestRun -> creditInvestRun.getOrderId()).collect(Collectors.toList());
        Specification<TransferBuyLog> tbls = Specifications
                .<TransferBuyLog>and()
                .eq("thirdTransferOrderId", orderIds.toArray())
                .build();
        List<TransferBuyLog> transferBuyLogList = transferBuyLogService.findList(tbls);
        Map<String, TransferBuyLog> transferBuyLogMaps = transferBuyLogList.stream().
                collect(Collectors.toMap(TransferBuyLog::getThirdTransferOrderId, Function.identity()));
        creditInvestRunList.forEach(creditInvestRun -> {
            String orderId = creditInvestRun.getOrderId();
            TransferBuyLog transferBuyLog = transferBuyLogMaps.get(orderId);
            transferBuyLog.setTransferAuthCode(creditInvestRun.getAuthCode());
        });
        transferBuyLogService.save(transferBuyLogList);
    }


    /**
     * 取消即信投标申请
     *
     * @param voCancelThirdTenderReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> cancelThirdTender(VoCancelThirdTenderReq voCancelThirdTenderReq) {
        Long tenderId = voCancelThirdTenderReq.getTenderId();
        Preconditions.checkNotNull(tenderId, "tenderThirdBizImpl cancelThirdTender: tenderId为空!");
        Tender tender = tenderService.findById(tenderId);
        Preconditions.checkNotNull(tender, "tenderThirdBizImpl cancelThirdTender: tender为空!");
        UserThirdAccount tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
        Preconditions.checkNotNull(tenderUserThirdAccount, "tenderThirdBizImpl cancelThirdTender: 投资人未开户!");
        Borrow borrow = borrowService.findById(tender.getBorrowId());
        Preconditions.checkNotNull(borrow, "tenderThirdBizImpl cancelThirdTender: tender为空!");

        /* 添加取消投标前置查询 */
        BidApplyQueryRequest bidApplyQueryRequest = new BidApplyQueryRequest();
        bidApplyQueryRequest.setAccountId(tenderUserThirdAccount.getAccountId());
        bidApplyQueryRequest.setChannel(ChannelContant.HTML);
        bidApplyQueryRequest.setOrgOrderId(tender.getThirdTenderOrderId());
        BidApplyQueryResponse bidApplyQueryResponse = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, bidApplyQueryRequest, BidApplyQueryResponse.class);
        /* 取消投标orderId */
        String orderId = JixinHelper.getOrderId(JixinHelper.TENDER_CANCEL_PREFIX);
        if (!JixinResultContants.SUCCESS.equals(bidApplyQueryResponse.getRetCode())) {
            String msg = ObjectUtils.isEmpty(bidApplyQueryResponse) ? "当前网络不稳定，请稍候重试" : bidApplyQueryResponse.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        } else if ("2".equals(bidApplyQueryResponse.getState()) || "4".equals(bidApplyQueryResponse.getState())) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, String.format("投标还款中不能取消借款 tenderId:%s", tenderId)));
        } else if (!"9".equals(bidApplyQueryResponse.getState())) {

            BidCancelReq request = new BidCancelReq();
            request.setAccountId(tenderUserThirdAccount.getAccountId());
            request.setTxAmount(StringHelper.formatDouble(tender.getValidMoney(), 100, false));
            request.setChannel(ChannelContant.HTML);
            request.setOrderId(orderId);
            request.setOrgOrderId(tender.getThirdTenderOrderId());
            request.setProductId(borrow.getProductId());
            request.setAcqRes(StringHelper.toString(tenderId));
            BidCancelResp response = jixinManager.send(JixinTxCodeEnum.BID_CANCEL, request, BidCancelResp.class);
            if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
            }
        }
        tender.setThirdTenderCancelOrderId(orderId);
        tenderService.save(tender);
        return ResponseEntity.ok(VoBaseResp.ok("取消成功!"));
    }


    /**
     * 投资人批次结束债权参数验证回调
     *
     * @return
     */
    public void thirdBatchCreditEndCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchCreditInvestRunCall batchCreditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });

        if (ObjectUtils.isEmpty(batchCreditInvestRunCall)) {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestRunCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long borrowId = NumberHelper.toLong(acqResMap.get("borrowId"));
        if (!JixinResultContants.SUCCESS.equals(batchCreditInvestRunCall.getRetCode())) {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("回调失败! msg:" + batchCreditInvestRunCall.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestRunCall.getBatchNo(), borrowId, 2, ThirdBatchLogContants.BATCH_CREDIT_END);
        } else {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestRunCall.getBatchNo(), borrowId, 1, ThirdBatchLogContants.BATCH_CREDIT_END);
        }

        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 投资人批次结束债权业务运行回调
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchCreditEndRunCall(HttpServletRequest request, HttpServletResponse
            response) throws Exception {
        BatchCreditInvestRunCall batchCreditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });
        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestRunCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        if (ObjectUtils.isEmpty(batchCreditInvestRunCall)) {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("请求体为空!");
            log.error("======================================================================================");
            log.error("======================================================================================");
        }

        if (!JixinResultContants.SUCCESS.equals(batchCreditInvestRunCall.getRetCode())) {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("回调失败! msg:" + batchCreditInvestRunCall.getRetMsg());
            log.error("======================================================================================");
            log.error("======================================================================================");
        } else {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("回调成功!");
            log.error("======================================================================================");
            log.error("======================================================================================");
        }

        //触发处理批次放款处理结果队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("borrowId")), batchCreditInvestRunCall.getBatchNo(),
                    ThirdBatchLogContants.BATCH_CREDIT_END, batchCreditInvestRunCall.getAcqRes(), GSON.toJson(batchCreditInvestRunCall));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }

        return ResponseEntity.ok("success");
    }
}
