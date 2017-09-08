package com.gofobao.framework.system.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_query.BatchQueryReq;
import com.gofobao.framework.api.model.batch_query.BatchQueryResp;
import com.gofobao.framework.asset.entity.AdvanceLog;
import com.gofobao.framework.asset.service.AdvanceLogService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.BooleanHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.system.vo.request.VoSendThirdBatch;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/7/14.
 */
@Service
@Slf4j
public class ThirdBatchLogBizImpl implements ThirdBatchLogBiz {

    final Gson gson = new GsonBuilder().create();
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private AdvanceLogService advanceLogService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private MqHelper mqHelper;


    /**
     * 发送即信批次处理
     *
     * @param voSendThirdBatch
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> sendThirdBatchDeal(VoSendThirdBatch voSendThirdBatch) {
        log.info("pc：触发发送即信批次处理");
        String paramStr = voSendThirdBatch.getParamStr();/* pc请求提前结清参数 */
        if (!SecurityHelper.checkSign(voSendThirdBatch.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc发送即信批次处理 签名验证不通过!"));
        }
        Map<String, String> paramMap = gson.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        /* sourceId */
        String thirdBatchLogId = paramMap.get("thirdBatchLogId");

        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("id",thirdBatchLogId)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
        if (CollectionUtils.isEmpty(thirdBatchLogList)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次记录不存在!"));
        }
        //批次日志记录
        ThirdBatchLog thirdBatchLog = thirdBatchLogList.get(0);
        log.info(String.format("thirdBatchLog 记录->%s", gson.toJson(thirdBatchLog)));

        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(170106),
                        MqConfig.BATCH_NO, StringHelper.toString("193522"),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()),
                        MqConfig.ACQ_RES, thirdBatchLogList.get(0).getAcqRes()
                );

        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq %s", gson.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
            return ResponseEntity.ok(VoBaseResp.ok("处理成功!"));
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq exception", e);
            return ResponseEntity.ok(VoBaseResp.ok("处理失败!"));
        }
    }

    /**
     * 更新批次日志状态
     *
     * @param batchNo
     * @return
     */
    public boolean updateBatchLogState(String batchNo, Long sourceId, int state) {
        ThirdBatchLog thirdBatchLog = thirdBatchLogService.findByBatchNoAndSourceId(batchNo, sourceId);
        if (ObjectUtils.isEmpty(thirdBatchLog)) {
            return false;
        }
        thirdBatchLog.setState(state);
        thirdBatchLogService.save(thirdBatchLog);
        return true;
    }

    /**
     * 获取有效的最后一条批次记录
     *
     * @param sourceId
     * @param type
     * @return
     */
    public ThirdBatchLog getValidLastBatchLog(String sourceId, Integer... type) {
        //查询最后一条提交的批次
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", sourceId)
                .in("state", 0, 1)
                .in("type", type)
                .build();
        Pageable pageable = new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "id"));
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls, pageable);
        if (!CollectionUtils.isEmpty(thirdBatchLogList)) {
            return thirdBatchLogList.get(0);
        }
        return null;
    }

    /**
     * 更据sourceId检查批次是否频繁提交
     *
     * @param sourceId
     * @return
     */
    public int checkBatchOftenSubmit(String sourceId, Integer... type) {
        //查询最后一条提交的批次
        ThirdBatchLog thirdBatchLog = getValidLastBatchLog(sourceId, type);
        if (ObjectUtils.isEmpty(thirdBatchLog)) {
            return ThirdBatchLogContants.VACANCY;
        }

        //判断这个批次是否处理成功
        BatchQueryReq req = new BatchQueryReq();
        req.setChannel(ChannelContant.HTML);
        req.setBatchNo(thirdBatchLog.getBatchNo());
        req.setBatchTxDate(DateHelper.dateToString(thirdBatchLog.getCreateAt(), DateHelper.DATE_FORMAT_YMD_NUM));
        BatchQueryResp resp = jixinManager.send(JixinTxCodeEnum.BATCH_QUERY, req, BatchQueryResp.class);
        if ((ObjectUtils.isEmpty(resp)) || (!JixinResultContants.SUCCESS.equals(resp.getRetCode()))) {
            log.error(ObjectUtils.isEmpty(resp) ? "当前网络不稳定，请稍候重试" : resp.getRetMsg());
            //修改批次日志状态
            thirdBatchLog.setState(2);
            thirdBatchLogService.save(thirdBatchLog);
            return ThirdBatchLogContants.AWAIT;
        }

        String state = resp.getBatchState();
        if (state.equals(ThirdBatchLogContants.DISPOSING) || state.equals(ThirdBatchLogContants.AWAIT_DISPOSE)) {
            return ThirdBatchLogContants.AWAIT;
        } else if (state.equals(ThirdBatchLogContants.PROCESSED)) {
            return ThirdBatchLogContants.SUCCESS;
        }

        return ThirdBatchLogContants.VACANCY;
    }

    /**
     * 校验本地资源回调状态
     *
     * @param sourceId
     * @param type
     * @return true 已处理  false 未处理
     */
    public boolean checkLocalSourceState(String sourceId, int type) {
        Specification<Borrow> bs = null;
        Specification<BorrowRepayment> brs = null;
        Specification<AdvanceLog> als = null;
        Specification<Transfer> ts = null;
        List<Borrow> borrowList = null;
        long rowNum = 0;
        switch (type) {
            case ThirdBatchLogContants.BATCH_CREDIT_INVEST: //投资人批次购买债权
                ts = Specifications
                        .<Transfer>and()
                        .eq("state", 2)
                        .eq("id", sourceId)
                        .build();
                List<Transfer> transferList = transferService.findList(ts);
                if (!CollectionUtils.isEmpty(transferList)) {
                    return true;
                }
                break;
            case ThirdBatchLogContants.BATCH_LEND_REPAY: //即信批次放款
                bs = Specifications
                        .<Borrow>and()
                        .eq("status", 3)
                        .eq("id", sourceId)
                        .build();
                borrowList = borrowService.findList(bs);
                if (CollectionUtils.isEmpty(borrowList)) {
                    return false;
                }

                if (!ObjectUtils.isEmpty(borrowList.get(0).getSuccessAt())) {//判断是否满标处理成功
                    return true;
                }
                break;
            case ThirdBatchLogContants.BATCH_REPAY: //即信批次还款
                brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("id", sourceId)
                        .eq("status", 1)
                        .build();
                rowNum = borrowRepaymentService.count(brs);
                if (rowNum >= 1) {
                    return true;
                }
                break;
            case ThirdBatchLogContants.BATCH_BAIL_REPAY: //名义借款人垫付
                als = Specifications
                        .<AdvanceLog>and()
                        .eq("repaymentId", sourceId)
                        .build();
                rowNum = advanceLogService.count(als);
                if (rowNum >= 1) {
                    return true;
                }
                break;
            case ThirdBatchLogContants.BATCH_REPAY_BAIL: //批次融资人还担保账户垫款
                als = Specifications
                        .<AdvanceLog>and()
                        .eq("repaymentId", sourceId)
                        .eq("status", 1)
                        .build();
                rowNum = advanceLogService.count(als);
                if (rowNum >= 1) {
                    return true;
                }
                break;
            case ThirdBatchLogContants.BATCH_CREDIT_END:
                //判断借款是否存在失败批次
                boolean flag = existFailureCreditEnd(NumberHelper.toLong(sourceId));
                if (!flag) {
                    return true;
                }
                break;
            case ThirdBatchLogContants.BATCH_REPAY_ALL: //提前结清批次还款
                bs = Specifications
                        .<Borrow>and()
                        .eq("id", sourceId)
                        .eq("status", 3)
                        .build();
                borrowList = borrowService.findList(bs);
                if (CollectionUtils.isEmpty(borrowList)) {
                    return false;
                }

                if (!ObjectUtils.isEmpty(borrowList.get(0).getCloseAt())) {
                    return true;
                }
                break;
            default:

        }
        return false;
    }

    /**
     * 判断借款是否存在失败批次
     *
     * @param borrowId
     * @return
     */
    private boolean existFailureCreditEnd(long borrowId) {
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .eq("borrowId", borrowId)
                .eq("status", 1)
                .build();
        List<Tender> tenderList = tenderService.findList(ts);
        if (CollectionUtils.isEmpty(tenderList)) {
            log.info("creditProvider buildCreditEndList: 借款" + borrowId + " 未找到投递成功债权！");
        }

        //筛选出已转让的投资记录
        tenderList.stream().filter(p -> p.getTransferFlag() == 2).forEach(tender -> {
            //查询转让标的
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .eq("tenderId", tender.getId())
                    .build();
            List<Borrow> borrowList = borrowService.findList(bs);
            if (!CollectionUtils.isEmpty(borrowList)) {
                existFailureCreditEnd(borrowList.get(0).getId());
            }
        });

        //判断投标记录里是否存在批次结束债权失败记录
        return tenderList.stream().filter(tender -> BooleanHelper.isFalse(tender.getThirdCreditEndFlag())).count() > 0;

    }
}
