package com.gofobao.framework.system.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchDealLogContants;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchDealLog;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchDealLogService;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.system.vo.request.VoFindLendRepayStatusListReq;
import com.gofobao.framework.system.vo.request.VoFindRepayStatusListReq;
import com.gofobao.framework.system.vo.response.VoFindLendRepayStatus;
import com.gofobao.framework.system.vo.response.VoFindRepayStatus;
import com.gofobao.framework.system.vo.response.VoViewFindLendRepayStatusListRes;
import com.gofobao.framework.system.vo.response.VoViewFindRepayStatusListRes;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/9/12.
 */
@Service
public class ThirdBatchDealLogBizImpl implements ThirdBatchDealLogBiz {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private ThirdBatchDealLogService thirdBatchDealLogService;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    /**
     * 查询还款状态集合
     *
     * @param voFindRepayStatusListReq
     * @return
     */
    public ResponseEntity<VoViewFindRepayStatusListRes> findRepayStatusList(VoFindRepayStatusListReq voFindRepayStatusListReq) {
        VoViewFindRepayStatusListRes repayStatusListRes = VoViewFindRepayStatusListRes.ok("查询成功!", VoViewFindRepayStatusListRes.class);

        /* 回款id */
        Long collectionId = voFindRepayStatusListReq.getCollectionId();

        /* 还款id */
        Long repaymentId = voFindRepayStatusListReq.getCollectionId();

        List<VoFindRepayStatus> voFindRepayStatusList = getVoFindRepayStatusList(collectionId, repaymentId);

        //修改参数
        repayStatusListRes.setVoFindRepayStatusList(voFindRepayStatusList);

        return ResponseEntity.ok(repayStatusListRes);
    }

    /**
     * @param collectionId
     * @return
     */
    public List<VoFindRepayStatus> getVoFindRepayStatusList(Long collectionId, Long repaymentId) {
        Preconditions.checkNotNull(ObjectUtils.isEmpty(collectionId) && ObjectUtils.isEmpty(repaymentId), "borrowCollection与repaymentId必须传其中一个!");
        BorrowRepayment borrowRepayment = null;
        Borrow borrow = null;
        if (!ObjectUtils.isEmpty(collectionId)) {
            /* 回款记录 */
            BorrowCollection borrowCollection = borrowCollectionService.findById(collectionId);
            Preconditions.checkNotNull(borrowCollection, "借款记录查询不存在!");
           /* 投标记录 */
            Tender tender = tenderService.findById(borrowCollection.getTenderId());
            Preconditions.checkNotNull(tender, "投标记录查询不存在!");
            /* 借款记录 */
            borrow = borrowService.findById(tender.getBorrowId());
            Preconditions.checkNotNull(tender, "借款记录查询不存在!");
            /* 查询当期还款记录 */
            Specification<BorrowRepayment> brs = Specifications
                    .<BorrowRepayment>and()
                    .eq("borrowId", borrow.getId())
                    .eq("order", borrowCollection.getOrder())
                    .build();
            List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
            Preconditions.checkState(!CollectionUtils.isEmpty(borrowRepaymentList), "还款记录不存在！");
            borrowRepayment = borrowRepaymentList.get(0);
        }

        if (!ObjectUtils.isEmpty(repaymentId)) {
            borrowRepayment = borrowRepaymentService.findById(repaymentId);
            borrow = borrowService.findById(borrowRepayment.getBorrowId());
        }

        //查询相关垫付、还款、提前结清
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", borrowRepayment.getId())
                .eq("type", ThirdBatchLogContants.BATCH_REPAY)
                .in("state", 0, 1, 3)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls, new Sort(Sort.Direction.DESC, "createAt"));
        if (ObjectUtils.isEmpty(thirdBatchLogList)) {//存在处理中还款操作
            tbls = Specifications
                    .<ThirdBatchLog>and()
                    .eq("sourceId", borrowRepayment.getId())
                    .eq("type", ThirdBatchLogContants.BATCH_BAIL_REPAY)
                    .in("state", 0, 1, 3)
                    .build();
            thirdBatchLogList = thirdBatchLogService.findList(tbls, new Sort(Sort.Direction.DESC, "createAt"));
            if (ObjectUtils.isEmpty(thirdBatchLogList)) {//存在处理中垫付操作
                tbls = Specifications
                        .<ThirdBatchLog>and()
                        .eq("sourceId", borrow.getId())
                        .eq("type", ThirdBatchLogContants.BATCH_REPAY_ALL)
                        .in("state", 0, 1, 3)
                        .build();
                thirdBatchLogList = thirdBatchLogService.findList(tbls, new Sort(Sort.Direction.DESC, "createAt"));
            }
        }
        /* 批次处理日志集合 */
        /* 批次处理记录 */
        ThirdBatchLog thirdBatchLog = null;
        Map<Integer/* state */, ThirdBatchDealLog> thirdBatchDealLogMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(thirdBatchLogList)) {
            //已完成批次
            List<ThirdBatchLog> successThirdBatchLogList = thirdBatchLogList.stream().filter(t -> t.getState().intValue() == 3).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(successThirdBatchLogList)) {
                thirdBatchLog = successThirdBatchLogList.get(0);
            } else {
                thirdBatchLog = thirdBatchLogList.get(0);
            }
            /*批次处理节点记录*/
            Specification<ThirdBatchDealLog> tbdls = Specifications
                    .<ThirdBatchDealLog>and()
                    .eq("batchId", thirdBatchLog.getId())
                    .build();
            List<ThirdBatchDealLog> thirdBatchDealLogList = thirdBatchDealLogService.findList(tbdls, new Sort(Sort.Direction.DESC, "batchId", "state", "createdAt"));
            thirdBatchDealLogList.stream().forEach(thirdBatchDealLog -> {
                thirdBatchDealLogMap.put(thirdBatchDealLog.getState(), thirdBatchDealLog);
            });
        }


        //不存在已完成批次，继续获取批次处理记录 0待处理 1.未通过 2.已通过
        List<VoFindRepayStatus> voFindRepayStatusList = new ArrayList<>();
        //填充放款状态数据
        fillFindRepayStatusData(borrowRepayment, thirdBatchLog, voFindRepayStatusList, thirdBatchDealLogMap);
        return voFindRepayStatusList;
    }

    /**
     * 填充放款状态数据
     *
     * @param borrowRepayment
     * @param thirdBatchLog
     * @param voFindRepayStatusList
     */
    public void fillFindRepayStatusData(BorrowRepayment borrowRepayment, ThirdBatchLog thirdBatchLog, List<VoFindRepayStatus> voFindRepayStatusList, Map<Integer/* state */, ThirdBatchDealLog> thirdBatchDealLogMap) {
        boolean flag = false;
        //第一步
        VoFindRepayStatus voFindRepayStatus = new VoFindRepayStatus();
        voFindRepayStatus.setName(ThirdBatchLogContants.REPAYMENT_FIRST_STEP);
        if (ObjectUtils.isEmpty(borrowRepayment.getRepayTriggerAt())) { // 0待处理
            voFindRepayStatus.setDateStr("- -");
            voFindRepayStatus.setState(0);
        } else if (!ObjectUtils.isEmpty(borrowRepayment.getRepayTriggerAt())) { // 2.已通过
            flag = true;
            voFindRepayStatus.setState(2);
            voFindRepayStatus.setDateStr(DateHelper.dateToString(borrowRepayment.getRepayTriggerAt()));
        } else { //1.未通过
            voFindRepayStatus.setDateStr(DateHelper.dateToString(borrowRepayment.getRepayTriggerAt()));
            voFindRepayStatus.setState(1);
        }
        voFindRepayStatusList.add(voFindRepayStatus);
        //第二步
        /* 批次处理节点记录 */
        ThirdBatchDealLog thirdBatchDealLog = thirdBatchDealLogMap.get(ThirdBatchDealLogContants.SEND_REQUEST);
        voFindRepayStatus = new VoFindRepayStatus();
        voFindRepayStatus.setName(ThirdBatchLogContants.REPAYMENT_SECOND_STEP);
        if (!flag) { // 0待处理
            voFindRepayStatus.setDateStr("- -");
            voFindRepayStatus.setState(0);
            flag = false;
        } else if ((!ObjectUtils.isEmpty(thirdBatchLog) && (thirdBatchLog.getState() == 5 || thirdBatchLog.getState() == 2 || thirdBatchLog.getState() == 4)) ||
                (!ObjectUtils.isEmpty(thirdBatchDealLog) && !thirdBatchDealLog.getStatus())) { //1.未通过
            Date date = ObjectUtils.isEmpty(borrowRepayment.getRepayTriggerAt()) ? (borrowRepayment.getUpdatedAt()) : borrowRepayment.getRepayTriggerAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 3);
            }

            voFindRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindRepayStatus.setState(1);
            flag = false;
        } else { // 2.已通过
            flag = true;
            Date date = ObjectUtils.isEmpty(borrowRepayment.getRepayTriggerAt()) ? (borrowRepayment.getUpdatedAt()) : borrowRepayment.getRepayTriggerAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 3);
            }
            voFindRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindRepayStatus.setState(2);
        }
        voFindRepayStatusList.add(voFindRepayStatus);
        //第三步
        /* 批次处理节点记录 */
        thirdBatchDealLog = thirdBatchDealLogMap.get(ThirdBatchDealLogContants.PARAM_CHECK);
        voFindRepayStatus = new VoFindRepayStatus();
        voFindRepayStatus.setName(ThirdBatchLogContants.REPAYMENT_THIRD_STEP);
        if (!flag || (!ObjectUtils.isEmpty(thirdBatchLog) && thirdBatchLog.getState() != 3 && thirdBatchLog.getState() != 5)) { // 0待处理
            voFindRepayStatus.setDateStr("- -");
            voFindRepayStatus.setState(0);
            flag = false;
        } else if ((!ObjectUtils.isEmpty(thirdBatchLog) && (thirdBatchLog.getState() == 5 || thirdBatchLog.getState() == 2 || thirdBatchLog.getState() == 4))
                || (!ObjectUtils.isEmpty(thirdBatchDealLog) && !thirdBatchDealLog.getStatus())) { //1.未通过
            Date date = ObjectUtils.isEmpty(borrowRepayment.getRepayTriggerAt()) ? (borrowRepayment.getUpdatedAt()) : borrowRepayment.getRepayTriggerAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 2);
            }
            voFindRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindRepayStatus.setState(1);
            flag = false;
        } else { // 2.已通过
            flag = true;
            Date date = ObjectUtils.isEmpty(borrowRepayment.getRepayTriggerAt()) ? (borrowRepayment.getUpdatedAt()) : borrowRepayment.getRepayTriggerAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 2);
            }
            voFindRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindRepayStatus.setState(2);
        }
        voFindRepayStatusList.add(voFindRepayStatus);
        //第四步
        /* 批次处理节点记录 */
        thirdBatchDealLog = thirdBatchDealLogMap.get(ThirdBatchDealLogContants.PROCESSED);
        voFindRepayStatus = new VoFindRepayStatus();
        voFindRepayStatus.setName(ThirdBatchLogContants.REPAYMENT_FOUR_STEP);
        if (!flag || (!ObjectUtils.isEmpty(thirdBatchLog) && thirdBatchLog.getState() != 3 && thirdBatchLog.getState() != 5)) { // 0待处理
            voFindRepayStatus.setDateStr("- -");
            voFindRepayStatus.setState(0);
        } else if ((!ObjectUtils.isEmpty(thirdBatchLog) && (thirdBatchLog.getState() == 5 || thirdBatchLog.getState() == 2 || thirdBatchLog.getState() == 4))
                || (!ObjectUtils.isEmpty(thirdBatchDealLog) && !thirdBatchDealLog.getStatus())) { //1.未通过
            Date date = ObjectUtils.isEmpty(borrowRepayment.getRepayTriggerAt()) ? (borrowRepayment.getUpdatedAt()) : borrowRepayment.getRepayTriggerAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 1);
            }
            voFindRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindRepayStatus.setState(1);
        } else { // 2.已通过
            Date date = ObjectUtils.isEmpty(borrowRepayment.getRepayTriggerAt()) ? (borrowRepayment.getUpdatedAt()) : borrowRepayment.getRepayTriggerAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 1);
            }
            voFindRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindRepayStatus.setState(2);
        }
        voFindRepayStatusList.add(voFindRepayStatus);
    }

    /**
     * 查询放款状态集合
     *
     * @param voFindLendRepayStatusListReq
     * @return
     */
    public ResponseEntity<VoViewFindLendRepayStatusListRes> findLendRepayStatusList(VoFindLendRepayStatusListReq voFindLendRepayStatusListReq) {
        VoViewFindLendRepayStatusListRes repayStatusListRes = VoViewFindLendRepayStatusListRes.ok("查询成功!", VoViewFindLendRepayStatusListRes.class);
        /*借款id*/
        long borrowId = voFindLendRepayStatusListReq.getBorrowId();
        /*借款对象*/
        Borrow borrow = borrowService.findById(borrowId);
        /*查询批次*/
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", borrow.getId())
                .eq("type", ThirdBatchLogContants.BATCH_LEND_REPAY)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls, new Sort(Sort.Direction.DESC, "createAt"));
        /* 批次处理记录 */
        ThirdBatchLog thirdBatchLog = null;
        /* 批次处理日志集合 */
        Map<Integer/* state */, ThirdBatchDealLog> thirdBatchDealLogMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(thirdBatchLogList)) {
            //已完成批次
            List<ThirdBatchLog> successThirdBatchLogList = thirdBatchLogList.stream().filter(t -> t.getState().intValue() == 3).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(successThirdBatchLogList)) {
                thirdBatchLog = successThirdBatchLogList.get(0);
            } else {
                thirdBatchLog = thirdBatchLogList.get(0);
            }
            /*批次处理节点记录*/
            Specification<ThirdBatchDealLog> tbdls = Specifications
                    .<ThirdBatchDealLog>and()
                    .eq("batchId", thirdBatchLog.getId())
                    .build();
            List<ThirdBatchDealLog> thirdBatchDealLogList = thirdBatchDealLogService.findList(tbdls, new Sort(Sort.Direction.DESC, "batchId", "state", "createdAt"));
            thirdBatchDealLogList.stream().forEach(thirdBatchDealLog -> {
                thirdBatchDealLogMap.put(thirdBatchDealLog.getState(), thirdBatchDealLog);
            });
        }

        //不存在已完成批次，继续获取批次处理记录 0待处理 1.未通过 2.已通过
        List<VoFindLendRepayStatus> voFindLendRepayStatusList = new ArrayList<>();
        //填充放款状态数据
        fillFindLendRepayStatusData(borrow, thirdBatchLog, voFindLendRepayStatusList, thirdBatchDealLogMap);

        //修改参数
        repayStatusListRes.setVoFindLendRepayStatusList(voFindLendRepayStatusList);
        return ResponseEntity.ok(repayStatusListRes);
    }

    /**
     * 填充放款状态数据
     *
     * @param borrow
     * @param thirdBatchLog
     * @param voFindLendRepayStatusList
     */
    public void fillFindLendRepayStatusData(Borrow borrow, ThirdBatchLog thirdBatchLog, List<VoFindLendRepayStatus> voFindLendRepayStatusList, Map<Integer/* state */, ThirdBatchDealLog> thirdBatchDealLogMap) {
        boolean flag = false;
        //第一步
        VoFindLendRepayStatus voFindLendRepayStatus = new VoFindLendRepayStatus();
        voFindLendRepayStatus.setName(ThirdBatchLogContants.BORROW_FIRST_STEP);
        if (ObjectUtils.isEmpty(borrow.getVerifyAt())) { // 0待处理
            voFindLendRepayStatus.setDateStr("- -");
            voFindLendRepayStatus.setState(0);
        } else if (borrow.getStatus() == 2) { //1.未通过
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(borrow.getVerifyAt()));
            voFindLendRepayStatus.setState(1);
        } else if (!ObjectUtils.isEmpty(borrow.getVerifyAt())) { // 2.已通过
            flag = true;
            voFindLendRepayStatus.setState(2);
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(borrow.getVerifyAt()));
        }
        voFindLendRepayStatusList.add(voFindLendRepayStatus);
        //第二步
        Set<Integer> borrowStatus = new HashSet<Integer>() {
        };
        borrowStatus.add(2);
        borrowStatus.add(4);
        borrowStatus.add(5);
        borrowStatus.add(6);
        voFindLendRepayStatus = new VoFindLendRepayStatus();
        voFindLendRepayStatus.setName(ThirdBatchLogContants.BORROW_SECOND_STEP);
        if (!flag || borrow.getMoneyYes() == borrow.getMoney()) { // 0待处理
            voFindLendRepayStatus.setDateStr("- -");
            voFindLendRepayStatus.setState(0);
            flag = false;
        } else if (borrowStatus.contains(borrow.getStatus().intValue())) { //1.未通过
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(borrow.getVerifyAt()));
            voFindLendRepayStatus.setState(1);
            flag = false;
        } else if (borrow.getMoneyYes().intValue() >= borrow.getMoney().intValue() && !ObjectUtils.isEmpty(borrow.getSuccessAt())) { // 2.已通过
            flag = true;
            voFindLendRepayStatus.setState(2);
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(borrow.getSuccessAt()));
        }
        voFindLendRepayStatusList.add(voFindLendRepayStatus);
        //第三步
        /* 批次处理节点记录 */
        ThirdBatchDealLog thirdBatchDealLog = thirdBatchDealLogMap.get(ThirdBatchDealLogContants.SEND_REQUEST);
        voFindLendRepayStatus = new VoFindLendRepayStatus();
        voFindLendRepayStatus.setName(ThirdBatchLogContants.BORROW_THIRD_STEP);
        if (!flag) { // 0待处理
            voFindLendRepayStatus.setDateStr("- -");
            voFindLendRepayStatus.setState(0);
            flag = false;
        } else if ((!ObjectUtils.isEmpty(thirdBatchLog) && (thirdBatchLog.getState() == 5 || thirdBatchLog.getState() == 2 || thirdBatchLog.getState() == 4)) ||
                (!ObjectUtils.isEmpty(thirdBatchDealLog) && !thirdBatchDealLog.getStatus())) { //1.未通过
            Date date = ObjectUtils.isEmpty(borrow.getRecheckAt()) ? borrow.getSuccessAt() : borrow.getRecheckAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 3);
            }

            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindLendRepayStatus.setState(1);
            flag = false;
        } else { // 2.已通过
            flag = true;
            Date date = ObjectUtils.isEmpty(borrow.getRecheckAt()) ? borrow.getSuccessAt() : borrow.getRecheckAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 3);
            }
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindLendRepayStatus.setState(2);
        }
        voFindLendRepayStatusList.add(voFindLendRepayStatus);
        //第四步
        /* 批次处理节点记录 */
        thirdBatchDealLog = thirdBatchDealLogMap.get(ThirdBatchDealLogContants.PARAM_CHECK);
        voFindLendRepayStatus = new VoFindLendRepayStatus();
        voFindLendRepayStatus.setName(ThirdBatchLogContants.BORROW_FOUR_STEP);
        if (!flag || (!ObjectUtils.isEmpty(thirdBatchLog) && thirdBatchLog.getState() != 3 && thirdBatchLog.getState() != 5)) { // 0待处理
            voFindLendRepayStatus.setDateStr("- -");
            voFindLendRepayStatus.setState(0);
            flag = false;
        } else if ((!ObjectUtils.isEmpty(thirdBatchLog) && (thirdBatchLog.getState() == 5 || thirdBatchLog.getState() == 2 || thirdBatchLog.getState() == 4))
                || (!ObjectUtils.isEmpty(thirdBatchDealLog) && !thirdBatchDealLog.getStatus())) { //1.未通过
            Date date = ObjectUtils.isEmpty(borrow.getRecheckAt()) ? borrow.getSuccessAt() : borrow.getRecheckAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 2);
            }
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindLendRepayStatus.setState(1);
            flag = false;
        } else { // 2.已通过
            flag = true;
            Date date = ObjectUtils.isEmpty(borrow.getRecheckAt()) ? borrow.getSuccessAt() : borrow.getRecheckAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 2);
            }
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindLendRepayStatus.setState(2);
        }
        voFindLendRepayStatusList.add(voFindLendRepayStatus);
        //第五步
        /* 批次处理节点记录 */
        thirdBatchDealLog = thirdBatchDealLogMap.get(ThirdBatchDealLogContants.PROCESSED);
        voFindLendRepayStatus = new VoFindLendRepayStatus();
        voFindLendRepayStatus.setName(ThirdBatchLogContants.BORROW_FIVE_STEP);
        if (!flag || (!ObjectUtils.isEmpty(thirdBatchLog) && thirdBatchLog.getState() != 3 && thirdBatchLog.getState() != 5)) { // 0待处理
            voFindLendRepayStatus.setDateStr("- -");
            voFindLendRepayStatus.setState(0);
        } else if ((!ObjectUtils.isEmpty(thirdBatchLog) && (thirdBatchLog.getState() == 5 || thirdBatchLog.getState() == 2 || thirdBatchLog.getState() == 4))
                || (!ObjectUtils.isEmpty(thirdBatchDealLog) && !thirdBatchDealLog.getStatus())) { //1.未通过
            Date date = ObjectUtils.isEmpty(borrow.getRecheckAt()) ? borrow.getSuccessAt() : borrow.getRecheckAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 1);
            }
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindLendRepayStatus.setState(1);
        } else { // 2.已通过
            Date date = ObjectUtils.isEmpty(borrow.getRecheckAt()) ? borrow.getSuccessAt() : borrow.getRecheckAt();
            if (!ObjectUtils.isEmpty(thirdBatchDealLog)) {
                date = thirdBatchDealLog.getCreatedAt();
            } else if (!ObjectUtils.isEmpty(thirdBatchLog)) {
                date = DateHelper.subMinutes(thirdBatchLog.getCreateAt(), 1);
            }
            voFindLendRepayStatus.setDateStr(DateHelper.dateToString(date));
            voFindLendRepayStatus.setState(2);
        }
        voFindLendRepayStatusList.add(voFindLendRepayStatus);
    }


    /**
     * 记录批次执行记录
     *
     * @param state
     * @param type
     * @return
     */
    public ThirdBatchDealLog recordThirdBatchDealLog(String batchNo, long sourceId, int state, boolean status, int type, String errorMsg) {
        ThirdBatchLog thirdBatchLog = thirdBatchLogService.findByBatchNoAndSourceIdAndType(batchNo, sourceId, type);
        if (ObjectUtils.isEmpty(thirdBatchLog)) {
            return null;
        }
        Date nowDate = new Date();

        /*前面一个*/
        int priorState = state - 1;
        if (priorState >= 0) {
            Specification<ThirdBatchDealLog> tbdls = Specifications
                    .<ThirdBatchDealLog>and()
                    .eq("batchId", thirdBatchLog.getId())
                    .eq("state", priorState)
                    .build();
            long count = thirdBatchDealLogService.count(tbdls);
            if (count < 1) {//前一个节点不存在记录
                ThirdBatchDealLog thirdBatchDealLog = new ThirdBatchDealLog();
                thirdBatchDealLog.setBatchId(thirdBatchLog.getId());
                thirdBatchDealLog.setState(priorState);
                thirdBatchDealLog.setStatus(true);
                thirdBatchDealLog.setType(type);
                thirdBatchDealLog.setCreatedAt(DateHelper.subMinutes(nowDate, 1));
                thirdBatchDealLog.setUpdatedAt(DateHelper.subMinutes(nowDate, 1));
                thirdBatchDealLogService.save(thirdBatchDealLog);
            }
        }

        ThirdBatchDealLog thirdBatchDealLog = new ThirdBatchDealLog();
        thirdBatchDealLog.setBatchId(thirdBatchLog.getId());
        thirdBatchDealLog.setState(state);
        thirdBatchDealLog.setStatus(status);
        thirdBatchDealLog.setType(type);
        thirdBatchDealLog.setErrorMsg(errorMsg);
        thirdBatchDealLog.setCreatedAt(nowDate);
        thirdBatchDealLog.setUpdatedAt(nowDate);
        return thirdBatchDealLogService.save(thirdBatchDealLog);
    }
}

