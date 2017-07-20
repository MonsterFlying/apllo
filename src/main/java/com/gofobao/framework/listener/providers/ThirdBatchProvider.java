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
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
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
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    private RepaymentBiz repaymentBiz;
    @Autowired
    private BorrowCollectionService borrowCollectionService;

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
        if (CollectionUtils.isEmpty(thirdBatchLogList)) {
            log.info("ThirdBatchProvider batchDeal:未查询到批次记录!");
            return false;
        }
        //主动查询未改变记录的批次状态，
        ThirdBatchLog thirdBatchLog = thirdBatchLogList.get(0);
        boolean flag = thirdBatchLogBiz.checkLocalSourceState(String.valueOf(thirdBatchLog.getSourceId()), thirdBatchLog.getType());//获取资源状态是否已完成状态
        if (flag) {
            log.info("资源状态：已发生改变!");
        }

        //批次存在失败批次，处理失败批次
        int pageIndex = 0, pageSize = 20, realSize = 0;
        List<DetailsQueryResp> detailsQueryRespList = new ArrayList<>();
        do {
            pageIndex++;
            //1.查询批次交易明细
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

        //赛选失败批次
        if (CollectionUtils.isEmpty(detailsQueryRespList)) {
            log.error(String.format("批量放款回调查询异常: %s", batchNo));
        }

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
                creditInvestDeal(sourceId, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_LEND_REPAY: //即信批次放款
                //=====================================================
                // 即信批次放款结果处理
                //=====================================================
                lendRepayDeal(sourceId, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_REPAY: //即信批次还款
                //=====================================================
                // 即信批次还款结果处理
                //=====================================================
                batchRepayDeal(acqRes, failureOrderIds, successOrderIds);
                break;
            case ThirdBatchLogContants.BATCH_BAIL_REPAY: //担保人垫付

                break;
            case ThirdBatchLogContants.BATCH_REPAY_BAIL: //批次融资人还担保账户垫款

                break;
            case ThirdBatchLogContants.BATCH_REPAY_ALL: //提前结清批次还款

                break;
            default:
        }

        return false;
    }

    /**
     * 批次还款处理
     *
     * @param acqRes
     * @param failureTRepayOrderIds
     * @param successTRepayOrderIds
     */
    private void batchRepayDeal(String acqRes, List<String> failureTRepayOrderIds, List<String> successTRepayOrderIds) throws Exception {

        if (CollectionUtils.isEmpty(failureTRepayOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次还款查询：查询未发现失败批次！");
            log.info("================================================================================");
        }

        //登记成功批次
        if (CollectionUtils.isEmpty(successTRepayOrderIds)) {
            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tRepayOrderId", successTRepayOrderIds.toArray())
                    .build();
            List<BorrowCollection> successTenderList = borrowCollectionService.findList(bcs);
            successTenderList.stream().forEach(tender -> {
                tender.setThirdRepayFlag(true);
            });
            borrowCollectionService.save(successTenderList);
        }

        //处理失败批次
        if (CollectionUtils.isEmpty(failureTRepayOrderIds)) { //不处理失败！

        }

        //==================================================================
        // 提前结清操作
        //==================================================================
        if (CollectionUtils.isEmpty(failureTRepayOrderIds)) {
            VoRepayReq voRepayReq = GSON.fromJson(acqRes, new TypeToken<VoRepayReq>() {
            }.getType());
            repaymentBiz.repayDeal(voRepayReq);
        }
    }

    /**
     * 批次放款处理
     *
     * @param failureThirdLendPayOrderIds
     * @param successThirdLendPayOrderIds
     */
    private void lendRepayDeal(Long borrowId, List<String> failureThirdLendPayOrderIds, List<String> successThirdLendPayOrderIds) throws Exception {
        Date nowDate = new Date();
        if (CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {
            log.info("================================================================================");
            log.info("即信批次放款查询：查询未发现失败批次！");
            log.info("================================================================================");
        }

        // 当明细中存在批量放款成功是
        // 直接更改记录为存款放款成功
        if (!CollectionUtils.isEmpty(successThirdLendPayOrderIds)) {
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdLendPayOrderId", successThirdLendPayOrderIds.toArray())
                    .build();
            List<Tender> successTenderList = tenderService.findList(ts);
            successTenderList.stream().forEach(tender -> {
                tender.setThirdTenderFlag(true);
            });
            tenderService.save(successTenderList);
        }

        boolean success = true;
        // 对于失败的债权, 先查询失败的标的ID
        if (!CollectionUtils.isEmpty(failureThirdLendPayOrderIds)) {
            success = false;
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdLendPayOrderId", failureThirdLendPayOrderIds.toArray())
                    .build();
            List<Tender> failureTenderList = tenderService.findList(ts);
            Map<Long, List<Tender>> borrowIdAndTenderMap = failureTenderList.stream().collect(Collectors.groupingBy(Tender::getBorrowId));
            Set<Long> borrowIdSet = borrowIdAndTenderMap.keySet();
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIdSet.toArray())
                    .build();
            List<Borrow> failureBorrowList = borrowService.findList(bs);

            // 对于失败的投标记录做以下操作
            // 1. 改变投标记录为失败
            // 2.解除资金冻结
            // 3.解除即信投标申请
            // 4.将标的改为可投状态, 减去投标金额
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

                    CapitalChangeEntity entity = new CapitalChangeEntity();
                    entity.setType(CapitalChangeEnum.Unfrozen);
                    entity.setUserId(tender.getUserId());
                    entity.setMoney(tender.getValidMoney());
                    entity.setRemark("借款 [" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "] 投标与存管通信失败，解除冻结资金。");
                    capitalChangeHelper.capitalChange(entity);
                }
                borrow.setTenderCount(borrow.getTenderCount() - failureNum.intValue());
                borrow.setMoneyYes(borrow.getMoneyYes() - failureAmount.intValue());
            }
            borrowService.save(failureBorrowList);
        }

        if (success) {
            Borrow borrow = borrowService.findById(borrowId);
            borrowBiz.notTransferBorrowAgainVerify(borrow);
        } else {
            log.info("非流转标复审失败!");
        }
    }

    /**
     * 批次债权转让失败批次处理
     *
     * @param successThirdTransferOrderIds
     * @param failureThirdTransferOrderIds
     */
    private void creditInvestDeal(Long borrowId, List<String> failureThirdTransferOrderIds, List<String> successThirdTransferOrderIds) throws Exception {
        Date nowDate = new Date();
        if (CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
            log.info("================================================================================");
            log.info("债权转让批次查询：查询未发现失败批次！");
            log.info("================================================================================");
        }

        //失败批次对应债权
        List<Long> borrowIdList = new ArrayList<>();
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .in("thirdTransferOrderId", failureThirdTransferOrderIds.toArray())
                .build();
        List<Tender> failureTenderList = tenderService.findList(ts);
        for (Tender tender : failureTenderList) {
            borrowIdList.add(tender.getBorrowId());
        }

        //成功批次对应债权
        ts = Specifications
                .<Tender>and()
                .in("thirdTransferOrderId", successThirdTransferOrderIds.toArray())
                .build();
        List<Tender> successTenderList = tenderService.findList(ts);
        for (Tender tender : successTenderList) {
            tender.setThirdTransferFlag(true);
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
        for (Borrow borrow : borrowList) {
            failAmount = 0;
            failNum = 0;
            if (!borrowIdSet.contains(borrow.getId())) {
                for (Tender tender : successTenderList) {
                    if (StringHelper.toString(borrow.getId()).equals(StringHelper.toString(tender.getBorrowId()))) {
                        failAmount += tender.getValidMoney(); //失败金额

                        //对冻结资金进行回滚
                        tender.setId(tender.getId());
                        tender.setStatus(2); // 取消状态
                        tender.setUpdatedAt(nowDate);

                        CapitalChangeEntity entity = new CapitalChangeEntity();
                        entity.setType(CapitalChangeEnum.Unfrozen);
                        entity.setUserId(tender.getUserId());
                        entity.setMoney(tender.getValidMoney());
                        entity.setRemark("借款 [" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "] 投标与存管通信失败，解除冻结资金。");
                        capitalChangeHelper.capitalChange(entity);

                    }
                }
                borrow.setTenderCount(borrow.getTenderCount() - failNum);
                borrow.setMoneyYes(borrow.getMoneyYes() - failAmount);
            }
        }
        borrowService.save(borrowList);
        tenderService.save(successTenderList);

        log.info("债权转让失败批次撤回成功!");

        //1.判断失败orderId集合为空
        //2.判断borrowId不为空
        boolean bool = false;
        if (CollectionUtils.isEmpty(failureThirdTransferOrderIds) && !ObjectUtils.isEmpty(borrowId)) {
            Borrow borrow = borrowService.findById(borrowId);
            try {
                bool = borrowBiz.transferBorrowAgainVerify(borrow);
            } catch (Throwable e) {
                log.error("非流转标复审异常:", e);
            }
        }
        if (bool) {
            log.info("非流转标复审成功!");
        } else {
            log.info("非流转标复审失败!");
        }
    }

}
