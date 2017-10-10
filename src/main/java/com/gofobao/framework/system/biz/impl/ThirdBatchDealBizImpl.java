package com.gofobao.framework.system.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.batch_details_query.DetailsQueryResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
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
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.contants.ThirdDealStatusContrants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchDealLogContants;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.entity.ThirdErrorRemark;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.system.service.ThirdErrorRemarkService;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoEndTransfer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gofobao.framework.listener.providers.NoticesMessageProvider.GSON;

/**
 * Created by Zeke on 2017/9/14.
 */
@Slf4j
@Service
public class ThirdBatchDealBizImpl implements ThirdBatchDealBiz {
    final Gson gson = new GsonBuilder().create();

    @Autowired
    ThirdBatchLogService thirdBatchLogService;
    @Autowired
    ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    JixinManager jixinManager;
    @Autowired
    TenderService tenderService;
    @Autowired
    BorrowService borrowService;
    @Autowired
    BorrowBiz borrowBiz;
    @Autowired
    TenderThirdBiz tenderThirdBiz;
    @Autowired
    RepaymentBiz repaymentBiz;
    @Autowired
    TransferBuyLogService transferBuyLogService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private TransferBiz transferBiz;
    @Autowired
    private ThirdErrorRemarkService thirdErrorRemarkService;
    @Autowired
    MqHelper mqHelper;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    BorrowCollectionService borrowCollectionService;
    @Autowired
    private ThirdBatchDealLogBiz thirdBatchDealLogBiz;
    @Autowired
    AssetChangeProvider assetChangeProvider;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private StatisticBiz statisticBiz;

    /**
     * 批次处理
     *
     * @param sourceId
     * @param batchNo
     * @param acqRes
     * @param batchResp
     * @return
     * @throws Exception
     */
    public boolean batchDeal(long sourceId, String batchNo, String acqRes, String batchResp) throws Exception {
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", sourceId)
                .eq("batchNo", batchNo)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
        Preconditions.checkState(!CollectionUtils.isEmpty(thirdBatchLogList), "批处理回调: 查询批处理记录为空");
        // 主动查询未改变记录的批次状态，
        ThirdBatchLog thirdBatchLog = thirdBatchLogList.get(0);
        boolean flag = thirdBatchLogBiz.checkLocalSourceState(String.valueOf(thirdBatchLog.getSourceId()), thirdBatchLog.getType());//获取资源状态是否已完成状态
        if (flag) {
            log.info("资源状态：已发生改变!");
        }

        // 批次存在失败批次，处理失败批次
        int pageIndex = 0, pageSize = 20, realSize = 0;
        List<DetailsQueryResp> detailsQueryRespList = new ArrayList<>();
        do {
            pageIndex++;
            // 1.查询批次交易明细
            BatchDetailsQueryReq batchDetailsQueryReq = new BatchDetailsQueryReq();
            batchDetailsQueryReq.setBatchNo(String.valueOf(batchNo));
            batchDetailsQueryReq.setBatchTxDate(DateHelper.dateToString(thirdBatchLogList.get(0).getCreateAt(), DateHelper.DATE_FORMAT_YMD_NUM));
            batchDetailsQueryReq.setType("0"); // 查询全部交易
            batchDetailsQueryReq.setPageNum(String.valueOf(pageIndex));
            batchDetailsQueryReq.setPageSize(String.valueOf(pageSize));
            batchDetailsQueryReq.setChannel(ChannelContant.HTML);
            BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, batchDetailsQueryReq, BatchDetailsQueryResp.class);
            if ((ObjectUtils.isEmpty(batchDetailsQueryResp)) || (!JixinResultContants.SUCCESS.equals(batchDetailsQueryResp.getRetCode()))) {
                log.error(ObjectUtils.isEmpty(batchDetailsQueryResp) ? "当前网络不稳定，请稍候重试" : batchDetailsQueryResp.getRetMsg());
            }
            List<DetailsQueryResp> detailsQueryRespsItemList = gson.fromJson(batchDetailsQueryResp.getSubPacks(), new TypeToken<List<DetailsQueryResp>>() {
            }.getType());
            if (CollectionUtils.isEmpty(detailsQueryRespsItemList)) {
                break;
            }
            realSize = detailsQueryRespsItemList.size();
            detailsQueryRespList.addAll(detailsQueryRespsItemList);
        } while (pageSize == realSize);

        //筛选失败批次
        Preconditions.checkNotNull(detailsQueryRespList, "批处理回调: 查询批次详细异常!");
        List<String> failureOrderIds = new ArrayList<>(); // 失败orderId
        List<String> successOrderIds = new ArrayList<>(); // 成功orderId
        List<String> failureErrorMsgList = new ArrayList<>();
        List<String> otherOrderIds = new ArrayList<>();//其它状态orderId
        detailsQueryRespList.forEach(obj -> {
            if ("F".equalsIgnoreCase(obj.getTxState())) {
                failureOrderIds.add(obj.getOrderId());
                failureErrorMsgList.add(obj.getFailMsg());
                log.error(String.format("批次处理,出现失败批次: %s", obj.getFailMsg()));
            } else if ("S".equalsIgnoreCase(obj.getTxState())) {
                successOrderIds.add(obj.getOrderId());
            } else {
                log.error(String.format("批次回调状态不明确,批次状态:%s", obj.getFailMsg()));
                otherOrderIds.add(obj.getOrderId());
            }
        });

        Preconditions.checkState(CollectionUtils.isEmpty(otherOrderIds), String.format("批次处理存在%s状态,程序暂停运行!", GSON.toJson(otherOrderIds)));

        //不存在失败批次进行后续操作
        try {
            Statistic statistic = null;
            switch (thirdBatchLog.getType()) {
                case ThirdBatchLogContants.BATCH_CREDIT_INVEST: // 投资人批次购买债权
                    // 批次债权转让结果处理
                    newCreditInvestDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                    break;
                case ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST:
                    //理财计划批次债权转让结果处理
                    financeCreditInvestDeal(batchNo, sourceId, failureOrderIds, successOrderIds, acqRes);
                    break;
                case ThirdBatchLogContants.BATCH_LEND_REPAY: // 即信批次放款
                    // 即信批次放款结果处理
                    statistic = lendRepayDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                    //即信批次放款结果处理总统计
                    statisticBiz.caculate(statistic);
                    break;
                case ThirdBatchLogContants.BATCH_FINANCE_LEND_REPAY:
                    // 即信批次放款结果处理
                    statistic = financeLendRepayDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                    //即信批次放款结果处理总统计
                    statisticBiz.caculate(statistic);
                    break;
                case ThirdBatchLogContants.BATCH_REPAY: //即信批次还款
                    // 即信批次还款结果处理
                    statistic = repayDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                    //提前结清批次还款总统计
                    statisticBiz.caculate(statistic);
                    break;
                case ThirdBatchLogContants.BATCH_BAIL_REPAY: //名义借款人垫付
                    // 即信批次名义借款人垫付处理
                    statistic = bailRepayDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                    //即信批次名义借款人垫付处理总统计
                    statisticBiz.caculate(statistic);
                    break;
                case ThirdBatchLogContants.BATCH_CREDIT_END: //批次结束债权
                    // 批次结束债权
                    creditEndDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                    break;
                case ThirdBatchLogContants.BATCH_REPAY_ALL: //提前结清批次还款
                    // 提前结清批次还款
                    statistic = repayAllDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                    //提前结清批次还款总统计
                    statisticBiz.caculate(statistic);
                    break;
                default:
            }
        } catch (Exception e) {
            log.error(String.format("批次处理异常:batchNo:%s,sourceId:%s,thi", batchNo, sourceId, thirdBatchLog.getType()), e);
            /*//判断是否有失败的记录，存在失败orderId添加失败日志
            ThirdErrorRemark remark = new ThirdErrorRemark();
            remark.setState(0);
            remark.setType(thirdBatchLog.getType());
            remark.setSourceId(sourceId);
            remark.setOldBatchNo(String.valueOf(batchNo));
            remark.setThirdRespStr(batchResp);
            remark.setThirdErrorMsg(GSON.toJson(failureErrorMsgList));
            remark.setErrorMsg(e.getMessage());
            remark.setCreatedAt(new Date());
            remark.setUpdatedAt(new Date());
            thirdErrorRemarkService.save(remark);*/

            throw new Exception(e);
        }

        return true;
    }

    /**
     * 批次结束债权处理
     *
     * @param borrowId
     * @param failureThirdCreditEndOrderIds
     * @param successThirdCreditEndOrderIds
     */
    @Transactional(rollbackFor = Exception.class)
    private void creditEndDeal(String batchNo, long borrowId, List<String> failureThirdCreditEndOrderIds, List<String> successThirdCreditEndOrderIds) {
        if (CollectionUtils.isEmpty(failureThirdCreditEndOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次还款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        //登记成功批次
        if (!CollectionUtils.isEmpty(successThirdCreditEndOrderIds)) {
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdCreditEndOrderId", successThirdCreditEndOrderIds.toArray())
                    .build();
            List<Tender> successTenderList = tenderService.findList(ts);
            successTenderList.stream().forEach(tender -> {
                tender.setThirdCreditEndFlag(true);
            });
            tenderService.save(successTenderList);
        }

        if (CollectionUtils.isEmpty(failureThirdCreditEndOrderIds)) {
            //更新批次日志状态
            thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), borrowId, 3, ThirdBatchLogContants.BATCH_CREDIT_END);
        }
    }

    /**
     * 提前结清批次还款处理
     *
     * @param borrowId
     * @param failureTRepayAllOrderIds
     * @param successTRepayAllOrderIds
     */
    @Transactional(rollbackFor = Exception.class)
    private Statistic repayAllDeal(String batchNo, long borrowId, List<String> failureTRepayAllOrderIds, List<String> successTRepayAllOrderIds) {
        Statistic statistic = new Statistic();
        if (CollectionUtils.isEmpty(failureTRepayAllOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次还款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        //登记成功批次
        Specification<BorrowCollection> bcs = null;
        if (!CollectionUtils.isEmpty(successTRepayAllOrderIds)) {
            bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tRepayOrderId", successTRepayAllOrderIds.toArray())
                    .build();
            List<BorrowCollection> successBorrowCollectionList = borrowCollectionService.findList(bcs);
            successBorrowCollectionList.stream().forEach(collection -> {
                collection.setThirdRepayFlag(true);
            });
            borrowCollectionService.save(successBorrowCollectionList);
        }


        // 批次还款处理(提前结清)
        if (CollectionUtils.isEmpty(failureTRepayAllOrderIds)) {
            //提前结清操作
            ResponseEntity<VoBaseResp> resp = null;
            try {
                resp = repaymentBiz.repayAllDeal(borrowId, batchNo, statistic);
            } catch (Exception e) {
                log.error("批次还款处理(提前结清)异常:", e);
            }
            if (resp.getBody().getState().getCode() != VoBaseResp.OK) {
                log.error("批次还款处理(提前结清)异常:" + resp.getBody().getState().getMsg());
            } else {
                //更新批次状态
                log.error("批次还款处理正常:" + resp.getBody().getState().getMsg());
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), borrowId, 3, ThirdBatchLogContants.BATCH_REPAY_ALL);
                //记录批次处理日志
                thirdBatchDealLogBiz.recordThirdBatchDealLog(batchNo, borrowId,
                        ThirdBatchDealLogContants.PROCESSED, true, ThirdBatchLogContants.BATCH_REPAY_ALL, "");
            }
        }
        return statistic;
    }

    /**
     * 批次名义借款人垫付处理
     *
     * @param repaymentId
     * @param failureTransferOrderIds
     * @param successTransferOrderIds
     */
    @Transactional(rollbackFor = Exception.class)
    private Statistic bailRepayDeal(String batchNo, long repaymentId, List<String> failureTransferOrderIds, List<String> successTransferOrderIds) throws Exception {
        Statistic statistic = new Statistic();
        if (CollectionUtils.isEmpty(failureTransferOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次还款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        //登记成功批次
        if (!CollectionUtils.isEmpty(successTransferOrderIds)) {
            Specification<TransferBuyLog> tbls = Specifications
                    .<TransferBuyLog>and()
                    .in("thirdTransferOrderId", successTransferOrderIds.toArray())
                    .build();
            List<TransferBuyLog> successTransferBuyLogList = transferBuyLogService.findList(tbls);
            successTransferBuyLogList.stream().forEach(transferBuyLog -> {
                transferBuyLog.setThirdTransferFlag(true);
            });
            transferBuyLogService.save(successTransferBuyLogList);
        }

        //查询失败日志
        if (!CollectionUtils.isEmpty(failureTransferOrderIds)) {
            //取消失败批次的债权转让记录与购买债权转让记录
            Specification<TransferBuyLog> tbls = Specifications
                    .<TransferBuyLog>and()
                    .in("thirdTransferOrderId", successTransferOrderIds.toArray())
                    .build();
            List<TransferBuyLog> failureTransferBuyLogList = transferBuyLogService.findList(tbls);
            Preconditions.checkState(!CollectionUtils.isEmpty(failureTransferBuyLogList), "购买债权转让记录不能为空!");
            List<Long> transferIds = failureTransferBuyLogList.stream().map(TransferBuyLog::getTransferId).collect(Collectors.toList());
            //查询债权转让记录
            Specification<Transfer> transferSpecification = Specifications
                    .<Transfer>and()
                    .in("id", transferIds.toArray())
                    .build();
            List<Transfer> transferList = transferService.findList(transferSpecification);
            Preconditions.checkState(!CollectionUtils.isEmpty(transferList), "债权转让记录不存在!");
            /* 投标id集合 */
            List<Long> tenderIds = transferList.stream().map(Transfer::getTenderId).collect(Collectors.toList());
            Map<Long/* 投标id */, Transfer> transferMaps = transferList.stream().collect(Collectors.toMap(Transfer::getTenderId, Function.identity()));
            /* 投标记录 */
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("id", tenderIds.toArray())
                    .build();
            List<Tender> tenderList = tenderService.findList(ts);
            Preconditions.checkState(!CollectionUtils.isEmpty(tenderList), "投标记录不存在!");
            tenderList.stream().forEach(tender -> {
                //更新tender状态
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
                    log.error("thirdBatchProvider bailRepayDeal error:", e);
                }
            });
            tenderService.save(tenderList);

            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_REPAYMENT);
            mqConfig.setTag(MqTagEnum.ADVANCE);
            mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 5));
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_REPAYMENT_ID, StringHelper.toString(repaymentId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("thirdBatchProvider bailRepayDeal send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("thirdBatchProvider bailRepayDeal send mq exception", e);
            }

            //更新批次日志状态
            updateThirdBatchLogState(batchNo, repaymentId, ThirdBatchLogContants.BATCH_BAIL_REPAY, 4);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(batchNo, repaymentId,
                    ThirdBatchDealLogContants.PROCESSED, false, ThirdBatchLogContants.BATCH_BAIL_REPAY, "");
        }


        // 批次名义借款人垫付操作

        if (CollectionUtils.isEmpty(failureTransferOrderIds)) {
            ResponseEntity<VoBaseResp> resp = repaymentBiz.newAdvanceDeal(repaymentId, batchNo, statistic);
            if (resp.getBody().getState().getCode() != VoBaseResp.OK) {
                log.error("批次名义借款人垫付操作：" + resp.getBody().getState().getMsg());
            } else {
                log.info("批次名义借款人垫付操作：" + resp.getBody().getState().getMsg());
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), repaymentId, 3, ThirdBatchLogContants.BATCH_BAIL_REPAY);
                //记录批次处理日志
                thirdBatchDealLogBiz.recordThirdBatchDealLog(batchNo, repaymentId,
                        ThirdBatchDealLogContants.PROCESSED, true, ThirdBatchLogContants.BATCH_BAIL_REPAY, "");
            }
        }
        return statistic;
    }

    /**
     * 批次还款处理
     *
     * @param failureTRepayOrderIds
     * @param successTRepayOrderIds
     */
    @Transactional(rollbackFor = Exception.class)
    private Statistic repayDeal(String batchNo, long repaymentId, List<String> failureTRepayOrderIds, List<String> successTRepayOrderIds) throws Exception {
        Statistic statistic = new Statistic();
        if (CollectionUtils.isEmpty(failureTRepayOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次还款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        //登记成功批次
        if (!CollectionUtils.isEmpty(successTRepayOrderIds)) {
            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tRepayOrderId", successTRepayOrderIds.toArray())
                    .build();
            List<BorrowCollection> successBorrowCollectionList = borrowCollectionService.findList(bcs);
            successBorrowCollectionList.stream().forEach(borrowCollection -> {
                borrowCollection.setThirdRepayFlag(true);
            });
            borrowCollectionService.save(successBorrowCollectionList);
        }


        // 批次还款处理
        if (!CollectionUtils.isEmpty(failureTRepayOrderIds)) {
            //更新即信放款状态 为处理失败!
            BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);
            borrowRepayment.setRepayStatus(ThirdDealStatusContrants.INDISPOSE);
            borrowRepayment.setUpdatedAt(new Date());
            borrowRepaymentService.save(borrowRepayment);
        }

        if (CollectionUtils.isEmpty(failureTRepayOrderIds)) {
            //更新即信放款状态 为处理失败!
            BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);
            borrowRepayment.setRepayStatus(ThirdDealStatusContrants.DISPOSED);
            borrowRepayment.setUpdatedAt(new Date());
            borrowRepaymentService.save(borrowRepayment);

            ResponseEntity<VoBaseResp> resp = repaymentBiz.newRepayDeal(repaymentId, batchNo, statistic);
            if (resp.getBody().getState().getCode() != VoBaseResp.OK) {
                log.error("批次还款处理:" + resp.getBody().getState().getMsg());
            } else {
                log.info("批次还款处理:" + resp.getBody().getState().getMsg());
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), repaymentId, 3, ThirdBatchLogContants.BATCH_REPAY);
                //记录批次处理日志
                thirdBatchDealLogBiz.recordThirdBatchDealLog(batchNo, repaymentId,
                        ThirdBatchDealLogContants.PROCESSED, true, ThirdBatchLogContants.BATCH_REPAY, "");
            }
        }
        return statistic;
    }

    /**
     * 理财计划批次放款处理
     *
     * @param failureThirdLendPayOrderIds
     * @param successThirdLendPayOrderIds
     */
    @Transactional(rollbackFor = Exception.class)
    private Statistic financeLendRepayDeal(String batchNo, long borrowId, List<String> failureThirdLendPayOrderIds, List<String> successThirdLendPayOrderIds) throws Exception {
        Date nowDate = new Date();
        Statistic statistic = new Statistic();
        if (CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {
            log.info("================================================================================");
            log.info("理财计划即信批次放款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        Gson gson = new Gson();
        // 当明细中存在批量放款成功是
        // 直接更改记录为存款放款成功
        if (!CollectionUtils.isEmpty(successThirdLendPayOrderIds)) {
            log.info(String.format("理财计划批次放款: 正确放款批次处理开始 %s", gson.toJson(successThirdLendPayOrderIds)));
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdLendPayOrderId", successThirdLendPayOrderIds.toArray())
                    .build();
            List<Tender> successTenderList = tenderService.findList(ts);
            successTenderList.stream().forEach(tender -> {
                tender.setThirdTenderFlag(true);
            });
            tenderService.save(successTenderList);
            log.info(String.format("理财计划批次放款: 正确放款批次处理结束 %s", gson.toJson(successThirdLendPayOrderIds)));
        }


        // 对于失败的债权, 先查询失败的标的ID
        if (!CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {
            log.info(String.format("理财计划批次放款: 错误放款批次处理开始 %s", gson.toJson(successThirdLendPayOrderIds)));
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdLendPayOrderId", failureThirdLendPayOrderIds.toArray())
                    .build();
            List<Tender> failureTenderList = tenderService.findList(ts);
            Preconditions.checkNotNull(failureTenderList, "理财计划正常批次放款回调: 查询失败投标记录为空");
            Map<Long/** borrowId */, List<Tender> /** borrowId 对应的投标记录*/> borrowIdAndTenderMap = failureTenderList
                    .stream()
                    .collect(Collectors.groupingBy(Tender::getBorrowId));

            Set<Long> borrowIdSet = failureTenderList.stream().map(Tender::getBorrowId).collect(Collectors.toSet());
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIdSet.toArray())
                    .build();
            List<Borrow> failureBorrowList = borrowService.findList(bs);
            for (Borrow borrow : failureBorrowList) {
                List<Tender> tenders = borrowIdAndTenderMap.get(borrow.getId());
                Long failureAmount = tenders.stream().mapToLong(t -> t.getValidMoney()).sum();  // 投标失败金额
                Long failureNum = new Long(tenders.size());  // 投标失败笔数
                for (Tender tender : tenders) {
                    VoCancelThirdTenderReq voCancelThirdTenderReq = new VoCancelThirdTenderReq();
                    voCancelThirdTenderReq.setTenderId(tender.getId());
                    ResponseEntity<VoBaseResp> resp = tenderThirdBiz.cancelThirdTender(voCancelThirdTenderReq);
                    if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) {
                        log.error(String.format("理财计划批量放款回调: 取消投标申请失败 %s msg:%s", gson.toJson(voCancelThirdTenderReq)
                                , resp.getBody().getState().getMsg()));
                    }

                    tender.setId(tender.getId());
                    tender.setStatus(2); // 取消状态
                    tender.setUpdatedAt(nowDate);

                    // 取消冻结
                    AssetChange assetChange = new AssetChange();
                    assetChange.setSourceId(tender.getId());
                    assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
                    assetChange.setMoney(tender.getValidMoney());
                    assetChange.setSeqNo(assetChangeProvider.getSeqNo());
                    assetChange.setRemark(String.format("存管系统审核投资标的[%s]资格失败, 解除资金冻结%s元", borrow.getName(), StringHelper.formatDouble(tender.getValidMoney() / 100D, true)));
                    assetChange.setType(AssetChangeTypeEnum.unfreeze);
                    assetChange.setUserId(tender.getUserId());
                    assetChange.setForUserId(tender.getUserId());
                    assetChangeProvider.commonAssetChange(assetChange);
                }
                tenderService.save(tenders);

                //更新借款数据
                borrow.setTenderCount(borrow.getTenderCount() - failureNum.intValue());
                borrow.setMoneyYes(borrow.getMoneyYes() - failureAmount.intValue());
                borrow.setSuccessAt(null);
                borrow.setUpdatedAt(nowDate);

            }
            //更新批次日志状态
            updateThirdBatchLogState(batchNo, borrowId, ThirdBatchLogContants.BATCH_FINANCE_LEND_REPAY, 4);
            borrowService.save(failureBorrowList);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(batchNo, borrowId, ThirdBatchDealLogContants.PROCESSED, false,
                    ThirdBatchLogContants.BATCH_LEND_REPAY, String.format("失败lendPayOrderId:%s", GSON.toJson(failureThirdLendPayOrderIds)));

            //改变批次放款状态 处理失败
            Borrow borrow = borrowService.findById(borrowId);
            borrow.setLendRepayStatus(ThirdDealStatusContrants.UNDISPOSED);
            borrowService.save(borrow);
        }

        if (CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {

            Borrow borrow = borrowService.findById(borrowId);
            log.info(String.format("理财计划正常标的放款回调: %s", gson.toJson(borrow)));
            boolean flag = borrowBiz.financeBorrowAgainVerify(borrow, batchNo, statistic);
            if (!flag) {
                log.error("理财计划标的放款失败！标的id：" + borrowId);
            } else {
                log.error("理财计划标的放款成功！标的id：" + borrowId);
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(batchNo, borrowId, 3, ThirdBatchLogContants.BATCH_FINANCE_LEND_REPAY);
                //记录批次处理日志
                thirdBatchDealLogBiz.recordThirdBatchDealLog(batchNo, borrowId, ThirdBatchDealLogContants.PROCESSED, true,
                        ThirdBatchLogContants.BATCH_LEND_REPAY, "");
            }
            //改变批次放款状态 处理成功
            borrow.setLendRepayStatus(ThirdDealStatusContrants.DISPOSED);
            borrow.setRecheckAt(new Date());
            borrowService.save(borrow);
        } else {
            log.info("理财计划非流转标复审失败!");
        }
        return statistic;
    }

    /**
     * 批次放款处理
     *
     * @param failureThirdLendPayOrderIds
     * @param successThirdLendPayOrderIds
     */
    @Transactional(rollbackFor = Exception.class)
    private Statistic lendRepayDeal(String batchNo, long borrowId, List<String> failureThirdLendPayOrderIds, List<String> successThirdLendPayOrderIds) throws Exception {
        Date nowDate = new Date();
        Statistic statistic = new Statistic();
        if (CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次放款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        Gson gson = new Gson();
        // 当明细中存在批量放款成功是
        // 直接更改记录为存款放款成功
        if (!CollectionUtils.isEmpty(successThirdLendPayOrderIds)) {
            log.info(String.format("批次放款: 正确放款批次处理开始 %s", gson.toJson(successThirdLendPayOrderIds)));
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdLendPayOrderId", successThirdLendPayOrderIds.toArray())
                    .build();
            List<Tender> successTenderList = tenderService.findList(ts);
            successTenderList.stream().forEach(tender -> {
                tender.setThirdTenderFlag(true);
            });
            tenderService.save(successTenderList);
            log.info(String.format("批次放款: 正确放款批次处理结束 %s", gson.toJson(successThirdLendPayOrderIds)));
        }


        // 对于失败的债权, 先查询失败的标的ID
        if (!CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {
            log.info(String.format("批次放款: 错误放款批次处理开始 %s", gson.toJson(successThirdLendPayOrderIds)));
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdLendPayOrderId", failureThirdLendPayOrderIds.toArray())
                    .build();
            List<Tender> failureTenderList = tenderService.findList(ts);
            Preconditions.checkNotNull(failureTenderList, "正常批次放款回调: 查询失败投标记录为空");
            Map<Long/** borrowId */, List<Tender> /** borrowId 对应的投标记录*/> borrowIdAndTenderMap = failureTenderList
                    .stream()
                    .collect(Collectors.groupingBy(Tender::getBorrowId));

            Set<Long> borrowIdSet = failureTenderList.stream().map(Tender::getBorrowId).collect(Collectors.toSet());
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIdSet.toArray())
                    .build();
            List<Borrow> failureBorrowList = borrowService.findList(bs);
            for (Borrow borrow : failureBorrowList) {
                List<Tender> tenders = borrowIdAndTenderMap.get(borrow.getId());
                Long failureAmount = tenders.stream().mapToLong(t -> t.getValidMoney()).sum();  // 投标失败金额
                Long failureNum = new Long(tenders.size());  // 投标失败笔数
                for (Tender tender : tenders) {
                    VoCancelThirdTenderReq voCancelThirdTenderReq = new VoCancelThirdTenderReq();
                    voCancelThirdTenderReq.setTenderId(tender.getId());
                    ResponseEntity<VoBaseResp> resp = tenderThirdBiz.cancelThirdTender(voCancelThirdTenderReq);
                    if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) {
                        log.error(String.format("批量放款回调: 取消投标申请失败 %s msg:%s", gson.toJson(voCancelThirdTenderReq)
                                , resp.getBody().getState().getMsg()));
                    }

                    tender.setId(tender.getId());
                    tender.setStatus(2); // 取消状态
                    tender.setUpdatedAt(nowDate);

                    // 取消冻结
                    AssetChange assetChange = new AssetChange();
                    assetChange.setSourceId(tender.getId());
                    assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
                    assetChange.setMoney(tender.getValidMoney());
                    assetChange.setSeqNo(assetChangeProvider.getSeqNo());
                    assetChange.setRemark(String.format("存管系统审核投资标的[%s]资格失败, 解除资金冻结%s元", borrow.getName(), StringHelper.formatDouble(tender.getValidMoney() / 100D, true)));
                    assetChange.setType(AssetChangeTypeEnum.unfreeze);
                    assetChange.setUserId(tender.getUserId());
                    assetChange.setForUserId(tender.getUserId());
                    assetChangeProvider.commonAssetChange(assetChange);
                }
                tenderService.save(tenders);

                // 发送取消债权通知
                sendCancelTender(nowDate, borrow, tenders);
                borrow.setTenderCount(borrow.getTenderCount() - failureNum.intValue());
                borrow.setMoneyYes(borrow.getMoneyYes() - failureAmount.intValue());
                borrow.setSuccessAt(null);
                borrow.setUpdatedAt(nowDate);

                //取消新手投标标识
                deleteUserNoviceTender(borrow, tenders);

            }
            //更新批次日志状态
            updateThirdBatchLogState(batchNo, borrowId, ThirdBatchLogContants.BATCH_LEND_REPAY, 4);
            borrowService.save(failureBorrowList);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(batchNo, borrowId, ThirdBatchDealLogContants.PROCESSED, false,
                    ThirdBatchLogContants.BATCH_LEND_REPAY, String.format("失败lendPayOrderId:%s", GSON.toJson(failureThirdLendPayOrderIds)));

            //改变批次放款状态 处理失败
            Borrow borrow = borrowService.findById(borrowId);
            borrow.setLendRepayStatus(ThirdDealStatusContrants.UNDISPOSED);
            borrowService.save(borrow);
        }

        if (CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {

            Borrow borrow = borrowService.findById(borrowId);
            log.info(String.format("正常标的放款回调: %s", gson.toJson(borrow)));
            boolean flag = borrowBiz.borrowAgainVerify(borrow, batchNo, statistic);
            if (!flag) {
                log.error("标的放款失败！标的id：" + borrowId);
            } else {
                log.error("标的放款成功！标的id：" + borrowId);
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(batchNo, borrowId, 3, ThirdBatchLogContants.BATCH_LEND_REPAY);
                //记录批次处理日志
                thirdBatchDealLogBiz.recordThirdBatchDealLog(batchNo, borrowId, ThirdBatchDealLogContants.PROCESSED, true,
                        ThirdBatchLogContants.BATCH_LEND_REPAY, "");
            }
            //改变批次放款状态 处理成功
            borrow.setLendRepayStatus(ThirdDealStatusContrants.DISPOSED);
            borrow.setRecheckAt(new Date());
            borrowService.save(borrow);
        } else {
            log.info("非流转标复审失败!");
        }
        return statistic;
    }

    /**
     * 取消新手投标标识
     *
     * @param borrow
     * @param tenderList
     */
    private void deleteUserNoviceTender(Borrow borrow, List<Tender> tenderList) throws Exception {
        for (Tender tender : tenderList) {
            log.info(String.format("取消新手投标标识"));
            UserCache userCache = userCacheService.findById(tender.getUserId());

            if (borrow.isTransfer() && (!BooleanUtils.toBoolean(userCache.getTenderTransfer()))) {
                userCache.setTenderTransfer(0);
            } else if ((borrow.getType() == 0) && (!BooleanUtils.toBoolean(userCache.getTenderTuijian()))) {
                userCache.setTenderTuijian(0);
            } else if ((borrow.getType() == 1) && (!BooleanUtils.toBoolean(userCache.getTenderJingzhi()))) {
                userCache.setTenderJingzhi(0);
            } else if ((borrow.getType() == 2) && (!BooleanUtils.toBoolean(userCache.getTenderMiao()))) {
                userCache.setTenderMiao(0);
            } else if ((borrow.getType() == 4) && (!BooleanUtils.toBoolean(userCache.getTenderQudao()))) {
                userCache.setTenderQudao(0);
            }
            userCacheService.save(userCache);
        }
    }

    /**
     * 即信验证投标失败, 取消投标记录. 发送站内信
     *
     * @param nowDate
     * @param transfer
     * @param transferBuyLogList
     */
    private void sendCancelTransfer(Date nowDate, Transfer transfer, List<TransferBuyLog> transferBuyLogList) {
        Set<Long> userIdSet = transferBuyLogList.stream().map(transferBuyLog -> transferBuyLog.getUserId()).collect(Collectors.toSet());
        String content = String.format("你所投资的借款[ %s ] 与存管通讯失败, 在 %s 已取消", transfer.getTitle(), DateHelper.nextDate(nowDate));
        userIdSet.forEach(userid -> {
            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(userid);
            notices.setRead(false);
            notices.setName("购买债权转让的借款失败");
            notices.setContent(content);
            notices.setType("system");
            notices.setCreatedAt(nowDate);
            notices.setUpdatedAt(nowDate);
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            try {
                log.info(String.format("ThirdBatchProvider creditInvestDeal send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("ThirdBatchProvider creditInvestDeal send mq exception", e);
            }
        });
    }

    /**
     * 即信验证投标失败, 取消投标记录. 发送站内信
     *
     * @param nowDate
     * @param borrow
     * @param tenders
     */
    private void sendCancelTender(Date nowDate, Borrow borrow, List<Tender> tenders) {
        Set<Long> userIdSet = tenders.stream().map(tender -> tender.getUserId()).collect(Collectors.toSet());
        String content = String.format("你所投资的借款[ %s ] 与存管通讯失败, 在 %s 已取消", borrow.getName(), DateHelper.dateToString(new Date()));
        userIdSet.forEach(userid -> {
            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(userid);
            notices.setRead(false);
            notices.setName("投资的借款失败");
            notices.setContent(content);
            notices.setType("system");
            notices.setCreatedAt(nowDate);
            notices.setUpdatedAt(nowDate);
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            try {
                log.info(String.format("ThirdBatchProvider creditInvestDeal send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("ThirdBatchProvider creditInvestDeal send mq exception", e);
            }
        });
    }

    /**
     * 理财计划债权转让处理
     *
     * @param batchNo
     * @param transferId
     * @param failureThirdTransferOrderIds
     * @param successThirdTransferOrderIds
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    private void financeCreditInvestDeal(String batchNo, long transferId, List<String> failureThirdTransferOrderIds,
                                         List<String> successThirdTransferOrderIds, String acqRes) throws Exception {
        Date nowDate = new Date();
        if (CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info("================================================================================");
            log.info("理财计划债权转让批次查询：未发现失败批次！transferId:" + transferId);
            log.info("================================================================================");
        }

        /*即信请求保留信息*/
        Map<String, Object> acqMap = GSON.fromJson(acqRes, TypeTokenContants.MAP_TOKEN);

        if (!CollectionUtils.isEmpty(successThirdTransferOrderIds)) {
            //成功批次对应债权
            Specification<TransferBuyLog> transferBuyLogSpecification = Specifications
                    .<TransferBuyLog>and()
                    .in("thirdTransferOrderId", successThirdTransferOrderIds.toArray())
                    .build();

            List<TransferBuyLog> successTransferList = transferBuyLogService.findList(transferBuyLogSpecification);
            successTransferList.stream().forEach(buyLogConsumer -> {
                //设置转让状态为true
                buyLogConsumer.setThirdTransferFlag(true);

            });

            transferBuyLogService.save(successTransferList);
        }

        //查询失败日志
        if (!CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info(String.format("理财计划批量债权回调: 取消失败债权购买: %s", gson.toJson(failureThirdTransferOrderIds)));
            //失败批次对应债权
            Specification<TransferBuyLog> tbls = Specifications
                    .<TransferBuyLog>and()
                    .in("thirdTransferOrderId", failureThirdTransferOrderIds.toArray())
                    .build();

            List<TransferBuyLog> failureTransferBuyLogList = transferBuyLogService.findList(tbls);
            Preconditions.checkNotNull(failureTransferBuyLogList, "摘取批次处理: 查询失败的投标记录不存在!");
            Set<Long> transferIdSet = failureTransferBuyLogList.stream().map(transferBuyLog -> transferBuyLog.getTransferId()).collect(Collectors.toSet());
            //3.挑选出失败有失败批次的债权转让
            Specification<Transfer> ts = Specifications
                    .<Transfer>and()
                    .in("id", transferIdSet.toArray())
                    .build();
            List<Transfer> transferList = transferService.findList(ts);
            Preconditions.checkNotNull(transferList, "理财计划债权批次回调处理: 查询债权转让记录不存在!");
            Map<Long, List<TransferBuyLog>> transferByLogMap = failureTransferBuyLogList.stream().collect(Collectors.groupingBy(TransferBuyLog::getTransferId));
            for (Transfer transfer : transferList) {
                List<TransferBuyLog> transferBuyLogList = transferByLogMap.get(transfer.getId());
                for (TransferBuyLog transferBuyLog : transferBuyLogList) {
                    transferBuyLog.setState(2);
                    transferBuyLog.setUpdatedAt(nowDate);

                    // 解除理财计划冻结资金
                    AssetChange assetChange = new AssetChange();
                    assetChange.setSourceId(transferBuyLog.getId());
                    assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
                    assetChange.setMoney(transferBuyLog.getValidMoney());
                    assetChange.setSeqNo(assetChangeProvider.getSeqNo());
                    assetChange.setRemark(String.format("存管系统审核债权转让[%s]不通过, 成功解冻资金%s元", transfer.getTitle(), StringHelper.formatDouble(transferBuyLog.getValidMoney() / 100D, true)));
                    assetChange.setType(AssetChangeTypeEnum.financePlanUnFreeze);
                    assetChange.setUserId(transferBuyLog.getUserId());
                    assetChangeProvider.commonAssetChange(assetChange);
                }

                transfer.setTenderCount(transfer.getTenderCount() - transferBuyLogList.size());
                long sum = transferBuyLogList.stream().mapToLong(transferBuyLog -> transferBuyLog.getValidMoney()).sum();  // 取消的总总债权
                transfer.setTransferMoneyYes(transfer.getTransferMoneyYes() - sum);
                transfer.setUpdatedAt(nowDate);
                transfer.setSuccessAt(null);
                transferService.save(transfer);
            }
            //更新批次日志状态
            updateThirdBatchLogState(batchNo, transferId, ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST, 4);
            transferService.save(transferList);
            transferBuyLogService.save(failureTransferBuyLogList);
        }

        //1.判断失败orderId集合为空
        //2.判断borrowId不为空
        if (CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info(String.format("理财计划批量债权转让复审transfer: %s", transferId));
            /*债权转让记录*/
            Transfer transfer = transferService.findById(transferId);
            /*理财计划是否是赎回*/
            boolean repurchaseFlag = Boolean.valueOf(StringHelper.toString(acqMap.get("isRepurchase")));
            ResponseEntity<VoBaseResp> resp = transferBiz.againFinanceVerifyTransfer(transferId, batchNo, repurchaseFlag);
            if ((resp.getBody().getState().getCode() == VoBaseResp.OK)) { //只有全部转让才会触发结束债权
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), transferId, 3, ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST);
                if (transfer.getTransferMoneyYes() >= transfer.getTransferMoney()) {
                    //推送队列结束债权
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
                    mqConfig.setTag(MqTagEnum.END_CREDIT_BY_TRANSFER);
                    mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 1));
                    ImmutableMap<String, String> body = ImmutableMap
                            .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(transfer.getBorrowId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
                    mqConfig.setMsg(body);
                    try {
                        log.info(String.format("thirdBatchProvider financeCreditInvestDeal send mq %s", GSON.toJson(body)));
                        mqHelper.convertAndSend(mqConfig);
                    } catch (Throwable e) {
                        log.error("thirdBatchProvider financeCreditInvestDeal send mq exception", e);
                    }
                }

                log.info("理财计划批量债权转让复审: 成功");
            } else {
                log.error("理财计划批量债权转让复审: 失败");
            }
        }
    }

    /**
     * 新版债权转让处理
     *
     * @param batchNo
     * @param transferId
     * @param failureThirdTransferOrderIds
     * @param successThirdTransferOrderIds
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    private void newCreditInvestDeal(String batchNo, long transferId, List<String> failureThirdTransferOrderIds,
                                     List<String> successThirdTransferOrderIds) throws Exception {
        Date nowDate = new Date();
        if (CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info("================================================================================");
            log.info("债权转让批次查询：未发现失败批次！transferId:" + transferId);
            log.info("================================================================================");
        }

        if (!CollectionUtils.isEmpty(successThirdTransferOrderIds)) {
            //成功批次对应债权
            Specification<TransferBuyLog> transferBuyLogSpecification = Specifications
                    .<TransferBuyLog>and()
                    .in("thirdTransferOrderId", successThirdTransferOrderIds.toArray())
                    .build();

            List<TransferBuyLog> successTransferList = transferBuyLogService.findList(transferBuyLogSpecification);
            successTransferList.stream().forEach(buyLogConsumer -> {
                //设置转让状态为true
                buyLogConsumer.setThirdTransferFlag(true);

            });

            transferBuyLogService.save(successTransferList);
        }

        //查询失败日志
        if (!CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info(String.format("批量债权回调: 取消失败债权购买: %s", gson.toJson(failureThirdTransferOrderIds)));
            //失败批次对应债权
            Specification<TransferBuyLog> tbls = Specifications
                    .<TransferBuyLog>and()
                    .in("thirdTransferOrderId", failureThirdTransferOrderIds.toArray())
                    .build();

            List<TransferBuyLog> failureTransferBuyLogList = transferBuyLogService.findList(tbls);
            Preconditions.checkNotNull(failureTransferBuyLogList, "摘取批次处理: 查询失败的投标记录不存在!");
            Set<Long> transferIdSet = failureTransferBuyLogList.stream().map(transferBuyLog -> transferBuyLog.getTransferId()).collect(Collectors.toSet());
            //3.挑选出失败有失败批次的债权转让
            Specification<Transfer> ts = Specifications
                    .<Transfer>and()
                    .in("id", transferIdSet.toArray())
                    .build();
            List<Transfer> transferList = transferService.findList(ts);
            Preconditions.checkNotNull(transferList, "债权批次回调处理: 查询债权转让记录不存在!");
            Map<Long, List<TransferBuyLog>> transferByLogMap = failureTransferBuyLogList.stream().collect(Collectors.groupingBy(TransferBuyLog::getTransferId));
            for (Transfer transfer : transferList) {
                List<TransferBuyLog> transferBuyLogList = transferByLogMap.get(transfer.getId());
                for (TransferBuyLog transferBuyLog : transferBuyLogList) {
                    transferBuyLog.setState(2);
                    transferBuyLog.setUpdatedAt(nowDate);

                    // 解除冻结资金
                    AssetChange assetChange = new AssetChange();
                    assetChange.setSourceId(transferBuyLog.getId());
                    assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
                    assetChange.setMoney(transferBuyLog.getValidMoney());
                    assetChange.setSeqNo(assetChangeProvider.getSeqNo());
                    assetChange.setRemark(String.format("存管系统审核债权转让[%s]不通过, 成功解冻资金%s元", transfer.getTitle(), StringHelper.formatDouble(transferBuyLog.getValidMoney() / 100D, true)));
                    assetChange.setType(AssetChangeTypeEnum.unfreeze);
                    assetChange.setUserId(transferBuyLog.getUserId());
                    assetChangeProvider.commonAssetChange(assetChange);
                }

                // 发送取消债权通知
                sendCancelTransfer(nowDate, transfer, transferBuyLogList);
                transfer.setTenderCount(transfer.getTenderCount() - transferBuyLogList.size());
                long sum = transferBuyLogList.stream().mapToLong(transferBuyLog -> transferBuyLog.getValidMoney()).sum();  // 取消的总总债权
                transfer.setTransferMoneyYes(transfer.getTransferMoneyYes() - sum);
                transfer.setUpdatedAt(nowDate);
                transfer.setSuccessAt(null);
                transferService.save(transfer);
            }
            //更新批次日志状态
            updateThirdBatchLogState(batchNo, transferId, ThirdBatchLogContants.BATCH_CREDIT_INVEST, 4);
            transferService.save(transferList);
            transferBuyLogService.save(failureTransferBuyLogList);
        }

        //1.判断失败orderId集合为空
        //2.判断borrowId不为空
        if (CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info(String.format("批量债权转让复审transfer: %s", transferId));
            ResponseEntity<VoBaseResp> resp = transferBiz.againVerifyTransfer(transferId, batchNo);
            if (resp.getBody().getState().getCode() == VoBaseResp.OK) {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), transferId, 3, ThirdBatchLogContants.BATCH_CREDIT_INVEST);

                Transfer transfer = transferService.findById(transferId);
                //推送队列结束债权
                MqConfig mqConfig = new MqConfig();
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
                mqConfig.setTag(MqTagEnum.END_CREDIT_BY_TRANSFER);
                mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 1));
                ImmutableMap<String, String> body = ImmutableMap
                        .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(transfer.getBorrowId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
                mqConfig.setMsg(body);
                try {
                    log.info(String.format("thirdBatchProvider creditInvestDeal send mq %s", GSON.toJson(body)));
                    mqHelper.convertAndSend(mqConfig);
                } catch (Throwable e) {
                    log.error("thirdBatchProvider creditInvestDeal send mq exception", e);
                }

                log.info("批量债权转让复审: 成功");
            } else {
                log.error("批量债权转让复审: 失败");
            }
        }
    }

    /**
     * 更新批次日志狀態
     *
     * @param batchNo
     * @param sourceId
     */
    private void updateThirdBatchLogState(String batchNo, long sourceId, int type, int state) {
        //更新third_batch_log状态
        Specification<ThirdBatchLog> thirdBatchLogSpecification = Specifications
                .<ThirdBatchLog>and()
                .eq("type", type)
                .eq("batchNo", batchNo)
                .eq("sourceId", sourceId)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(thirdBatchLogSpecification);
        if (!CollectionUtils.isEmpty(thirdBatchLogList)) {
            ThirdBatchLog thirdBatchLog = thirdBatchLogList.get(0);
            thirdBatchLog.setState(4);
            thirdBatchLog.setUpdateAt(new Date());
            thirdBatchLogService.save(thirdBatchLog);
        }
    }
}
