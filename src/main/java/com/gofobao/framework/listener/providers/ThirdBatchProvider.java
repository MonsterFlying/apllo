package com.gofobao.framework.listener.providers;

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
import com.gofobao.framework.borrow.vo.request.VoRepayAll;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.VoAdvanceCall;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.gofobao.framework.listener.providers.NoticesMessageProvider.GSON;

/**
 * Created by Zeke on 2017/7/18.
 */
@Component
@Slf4j
public class ThirdBatchProvider {

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
    CapitalChangeHelper capitalChangeHelper;
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
    MqHelper mqHelper;

    @Autowired
    BorrowCollectionService borrowCollectionService;

    @Autowired
    AssetChangeProvider assetChangeProvider ;


    /**
     * 批次处理
     *
     * @param msg
     * @return
     */
    public boolean batchDeal(Map<String, String> msg) throws Exception {
        Long sourceId = NumberHelper.toLong(msg.get(MqConfig.SOURCE_ID));//batchLog sourceId
        Long batchNo = NumberHelper.toLong(msg.get(MqConfig.BATCH_NO));//batchLog batchNo
        String acqRes = msg.get(MqConfig.ACQ_RES);

        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", sourceId)
                .eq("batchNo", batchNo)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
        Preconditions.checkNotNull(thirdBatchLogList, "批处理回调: 查询批处理记录为空");
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
        detailsQueryRespList.forEach(list -> detailsQueryRespList.forEach(obj -> {
            if ("F".equalsIgnoreCase(obj.getTxState())) {
                failureOrderIds.add(obj.getOrderId());
                log.error(String.format("放款失败: %s", obj.getFailMsg()));
            } else if ("S".equalsIgnoreCase(obj.getTxState())) {
                successOrderIds.add(obj.getOrderId());
            } else {
                log.error("批次回调状态不明确");
            }
        }));

        //不存在失败批次进行后续操作
        switch (thirdBatchLog.getType()) {
            case ThirdBatchLogContants.BATCH_CREDIT_INVEST: //投资人批次购买债权
                //=====================================================
                // 批次债权转让结果处理
                //=====================================================
                newCreditInvestDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_LEND_REPAY: //即信批次放款
                //=====================================================
                // 即信批次放款结果处理
                //=====================================================
                lendRepayDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_REPAY: //即信批次还款
                //=====================================================
                // 即信批次还款结果处理
                //=====================================================
                repayDeal(batchNo, sourceId, acqRes, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_BAIL_REPAY: //担保人垫付
                //=====================================================
                // 即信批次担保人垫付处理
                //=====================================================
                bailRepayDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_REPAY_BAIL: //批次融资人还担保账户垫款
                //=====================================================
                // 即信批次融资人还担保账户垫款处理
                //=====================================================
                repayBailDeal(batchNo, sourceId, acqRes, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_CREDIT_END: //批次结束债权
                //=====================================================
                // 批次结束债权
                //=====================================================
                creditEndDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_REPAY_ALL: //提前结清批次还款
                //======================================================
                // 提前结清批次还款
                //======================================================
                repayAllDeal(batchNo, sourceId, failureOrderIds, successOrderIds);
                break;
            default:
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
    private void creditEndDeal(long batchNo, long borrowId, List<String> failureThirdCreditEndOrderIds, List<String> successThirdCreditEndOrderIds) {
        if (CollectionUtils.isEmpty(failureThirdCreditEndOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次还款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        //登记成功批次
        Specification<Tender> ts = null;
        if (!CollectionUtils.isEmpty(successThirdCreditEndOrderIds)) {
            ts = Specifications
                    .<Tender>and()
                    .in("thirdCreditEndOrderId", successThirdCreditEndOrderIds.toArray())
                    .build();
            List<Tender> successTenderList = tenderService.findList(ts);
            successTenderList.stream().forEach(collection -> {
                collection.setThirdCreditEndFlag(true);
            });
            tenderService.save(successTenderList);
        }

        //处理失败批次
        //5分钟处理一次
        if (!CollectionUtils.isEmpty(failureThirdCreditEndOrderIds)) {
            //推送队列结束债权
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
            mqConfig.setTag(MqTagEnum.END_CREDIT_ALL);
            mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 5));
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("repaymentBizImpl repayDeal send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("repaymentBizImpl repayDeal send mq exception", e);
            }
        }

        if (CollectionUtils.isEmpty(failureThirdCreditEndOrderIds)) {
            //更新批次日志状态
            thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), borrowId, 3);
        }
    }

    /**
     * 批次借款人还代偿处理
     *
     * @param borrowId
     * @param failureTRepayAllOrderIds
     * @param successTRepayAllOrderIds
     */
    private void repayAllDeal(long batchNo, long borrowId, List<String> failureTRepayAllOrderIds, List<String> successTRepayAllOrderIds) {
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

        //处理失败批次
        //五分钟处理一次
        if (!CollectionUtils.isEmpty(failureTRepayAllOrderIds)) {
            //推送队列结束债权
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_REPAYMENT);
            mqConfig.setTag(MqTagEnum.REPAY_ALL);
            mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 5));
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("thirdBatchProvider repayAllDeal send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("thirdBatchProvider repayAllDeal send mq exception", e);
            }
        }

        //==================================================================
        // 批次还款处理(提前结清)
        //==================================================================
        if (CollectionUtils.isEmpty(failureTRepayAllOrderIds)) {
            //提前结清操作
            VoRepayAll voRepayAll = new VoRepayAll();
            voRepayAll.setBorrowId(borrowId);
            ResponseEntity<VoBaseResp> resp = borrowBiz.repayAll(voRepayAll);
            if (resp.getBody().getState().getCode() != VoBaseResp.OK) {
                log.error("批次还款处理(提前结清)异常:" + resp.getBody().getState().getMsg());
            } else {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), borrowId, 3);
            }
        }
    }

    /**
     * 批次融资人还担保账户垫款
     *
     * @param acqRes
     * @param failureTRepayBailOrderIds
     * @param successTRepayBailOrderIds
     */
    private void repayBailDeal(long batchNo, long repaymentId, String acqRes, List<String> failureTRepayBailOrderIds, List<String> successTRepayBailOrderIds) {

        if (CollectionUtils.isEmpty(failureTRepayBailOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次还款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        //登记成功批次
        if (!CollectionUtils.isEmpty(successTRepayBailOrderIds)) {
            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tRepayBailOrderId", successTRepayBailOrderIds.toArray())
                    .build();
            List<BorrowCollection> successBorrowCollectionList = borrowCollectionService.findList(bcs);
            successBorrowCollectionList.stream().forEach(borrowCollection -> {
                borrowCollection.setThirdRepayBailFlag(true);
            });
            borrowCollectionService.save(successBorrowCollectionList);
        }

        //处理失败批次
        if (!CollectionUtils.isEmpty(failureTRepayBailOrderIds)) { //不处理失败！
            //推送队列结束债权
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_REPAYMENT);
            mqConfig.setTag(MqTagEnum.REPAY_ADVANCE);
            mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 5));
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_REPAYMENT_ID, StringHelper.toString(repaymentId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("thirdBatchProvider repayBailDeal send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("thirdBatchProvider repayBailDeal send mq exception", e);
            }

        }

        //==================================================================
        // 批次还款处理
        //==================================================================
        if (CollectionUtils.isEmpty(failureTRepayBailOrderIds)) {
            VoRepayReq voRepayReq = GSON.fromJson(acqRes, new TypeToken<VoRepayReq>() {
            }.getType());
            try {
                ResponseEntity<VoBaseResp> resp = repaymentBiz.repayDeal(voRepayReq);
                if (!ObjectUtils.isEmpty(resp)) {
                    log.error("批次融资人还担保账户垫款：" + resp.getBody().getState().getMsg());
                } else {
                    //更新批次状态
                    thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), repaymentId, 3);
                }
            } catch (Exception e) {
                log.error("ThirdBatchProvider repayBailDeal error:", e);
            }
        }
    }

    /**
     * 批次担保人代偿处理
     *
     * @param repaymentId
     * @param failureTBailRepayOrderIds
     * @param successTBailRepayOrderIds
     */
    private void bailRepayDeal(long batchNo, long repaymentId, List<String> failureTBailRepayOrderIds, List<String> successTBailRepayOrderIds) throws Exception {
        if (CollectionUtils.isEmpty(failureTBailRepayOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次还款查询：未发现失败批次！");
            log.info("================================================================================");
        }

        //登记成功批次
        if (!CollectionUtils.isEmpty(successTBailRepayOrderIds)) {
            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tBailRepayOrderId", successTBailRepayOrderIds.toArray())
                    .build();
            List<BorrowCollection> successBorrowCollectionList = borrowCollectionService.findList(bcs);
            successBorrowCollectionList.stream().forEach(borrowCollection -> {
                borrowCollection.setThirdBailRepayFlag(true);
            });
            borrowCollectionService.save(successBorrowCollectionList);
        }

        //处理失败批次
        if (!CollectionUtils.isEmpty(failureTBailRepayOrderIds)) {
            //推送队列结束债权
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
        }

        //==================================================================
        // 批次担保人代偿操作
        //==================================================================
        if (CollectionUtils.isEmpty(failureTBailRepayOrderIds)) {
            VoAdvanceCall voAdvanceCall = new VoAdvanceCall();
            voAdvanceCall.setRepaymentId(repaymentId);
            ResponseEntity<VoBaseResp> resp = repaymentBiz.advanceDeal(voAdvanceCall);
            if (!ObjectUtils.isEmpty(resp)) {
                log.error("批次担保人代偿操作：" + resp.getBody().getState().getMsg());
            } else {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), repaymentId, 3);
            }
        }
    }

    /**
     * 批次还款处理
     *
     * @param acqRes
     * @param failureTRepayOrderIds
     * @param successTRepayOrderIds
     */
    private void repayDeal(long batchNo, long repaymentId, String acqRes, List<String> failureTRepayOrderIds, List<String> successTRepayOrderIds) throws Exception {

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

        //处理失败批次
        if (!CollectionUtils.isEmpty(failureTRepayOrderIds)) { //不处理失败！
            log.info(String.format("批量还款出现还款失败: %s", gson.toJson(failureTRepayOrderIds)));
            //推送队列结束债权
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_REPAYMENT);
            mqConfig.setTag(MqTagEnum.REPAY);
            mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 5));
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_REPAYMENT_ID, StringHelper.toString(repaymentId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("thirdBatchProvider repayDeal send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("thirdBatchProvider repayDeal send mq exception", e);
            }
        }

        //==================================================================
        // 批次还款处理
        //==================================================================
        if (CollectionUtils.isEmpty(failureTRepayOrderIds)) {
            VoRepayReq voRepayReq = GSON.fromJson(acqRes, new TypeToken<VoRepayReq>() {
            }.getType());
            ResponseEntity<VoBaseResp> resp = repaymentBiz.repayDeal(voRepayReq);
            if (resp.getBody().getState().getCode() != VoBaseResp.OK) {
                log.error("批次还款处理:" + resp.getBody().getState().getMsg());
            } else {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), repaymentId, 3);
            }
        }
    }

    /**
     * 批次放款处理
     *
     * @param failureThirdLendPayOrderIds
     * @param successThirdLendPayOrderIds
     */
    private void lendRepayDeal(long batchNo, long borrowId, List<String> failureThirdLendPayOrderIds, List<String> successThirdLendPayOrderIds) throws Exception {
        Date nowDate = new Date();
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
            Map<Long/** borrowId */, List<Tender> /** borrowid 对应的投标记录*/> borrowIdAndTenderMap = failureTenderList
                    .stream()
                    .collect(Collectors.groupingBy(Tender::getBorrowId));

            Set<Long> borrowIdSet = borrowIdAndTenderMap.keySet();
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
                        log.error(String.format("批量放款回调: 取消投标申请失败 %s", gson.toJson(voCancelThirdTenderReq)));
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
                    assetChange.setType(AssetChangeTypeEnum.unfreeze) ;
                    assetChange.setUserId(tender.getUserId());
                    assetChange.setForUserId(tender.getUserId());
                }

                // 发送取消债权通知
                sendCancelTender(nowDate, borrow, tenders);
                borrow.setTenderCount(borrow.getTenderCount() - failureNum.intValue());
                borrow.setMoneyYes(borrow.getMoneyYes() - failureAmount.intValue());
                borrow.setUpdatedAt(nowDate);
            }
            borrowService.save(failureBorrowList);
        }

        if (CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {
            Borrow borrow = borrowService.findById(borrowId);
            log.info(String.format("正常标的放款回调: %s", gson.toJson(borrow)));
            boolean flag = borrowBiz.notTransferBorrowAgainVerify(borrow);
            if (!flag) {
                log.error("标的放款失败！标的id：" + borrowId);
            } else {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), borrowId, 3);
            }
        } else {
            log.info("非流转标复审失败!");
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
        String content = String.format("你所投资的借款[ %s ] 与存管通讯失败, 在 %s 已取消", BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()), DateHelper.nextDate(nowDate));
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
     * 新版债权转让处理
     *
     * @param batchNo
     * @param transferId
     * @param failureThirdTransferOrderIds
     * @param successThirdTransferOrderIds
     * @throws Exception
     */
    private void newCreditInvestDeal(long batchNo, long transferId, List<String> failureThirdTransferOrderIds, List<String> successThirdTransferOrderIds) throws Exception {
        Date nowDate = new Date();
        if (CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info("================================================================================");
            log.info("债权转让批次查询：未发现失败批次！transferId:" + transferId);
            log.info("================================================================================");
        }

        if (!CollectionUtils.isEmpty(successThirdTransferOrderIds)) {
            //成功批次对应债权
            Specification<TransferBuyLog> tbls = Specifications
                    .<TransferBuyLog>and()
                    .in("thirdTransferOrderId", successThirdTransferOrderIds.toArray())
                    .build();

            List<TransferBuyLog> successTransferList = transferBuyLogService.findList(tbls);
            successTransferList.stream().forEach(transferBuyLog -> {
                transferBuyLog.setThirdTransferFlag(true);
            });
            transferBuyLogService.save(successTransferList);
        }

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

                    //解除购买债权转让冻结资金
                    CapitalChangeEntity entity = new CapitalChangeEntity();
                    entity.setType(CapitalChangeEnum.Unfrozen);
                    entity.setUserId(transferBuyLog.getUserId());
                    entity.setMoney(transferBuyLog.getValidMoney());
                    entity.setRemark("借款 [" + transfer.getTitle() + "] 投标与存管通信失败，解除冻结资金。");
                    capitalChangeHelper.capitalChange(entity);
                }

                // 发送取消债权通知
                sendCancelTransfer(nowDate, transfer, transferBuyLogList);

                transfer.setTenderCount(transfer.getTenderCount() - transferBuyLogList.size());
                long sum = transferBuyLogList.stream().mapToLong(transferBuyLog -> transferBuyLog.getValidMoney()).sum();  // 取消的总总债权
                transfer.setTransferMoneyYes(transfer.getTransferMoneyYes() - sum);
                transfer.setUpdatedAt(nowDate);
            }

            transferService.save(transferList);
            transferBuyLogService.save(failureTransferBuyLogList);
        }

        //1.判断失败orderId集合为空
        //2.判断borrowId不为空
        if (CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info(String.format("批量债权转让复审transfer: %s", transferId));
            ResponseEntity<VoBaseResp> resp = transferBiz.againVerifyTransfer(transferId,batchNo);
            if (resp.getBody().getState().getCode() == VoBaseResp.OK) {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(String.valueOf(batchNo), transferId, 3);

                //推送队列结束债权
                MqConfig mqConfig = new MqConfig();
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
                mqConfig.setTag(MqTagEnum.END_CREDIT_BY_TRANSFER);
                mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 1));
                ImmutableMap<String, String> body = ImmutableMap
                        .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(transferId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
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

}
