package com.gofobao.framework.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeReq;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeResp;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeReq;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeResp;
import com.gofobao.framework.api.model.batch_repay.*;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryRequest;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResponse;
import com.gofobao.framework.api.model.debt_register.DebtRegisterRequest;
import com.gofobao.framework.api.model.debt_register.DebtRegisterResponse;
import com.gofobao.framework.api.model.debt_register_cancel.DebtRegisterCancelReq;
import com.gofobao.framework.api.model.debt_register_cancel.DebtRegisterCancelResp;
import com.gofobao.framework.api.model.trustee_pay.TrusteePayReq;
import com.gofobao.framework.api.model.trustee_pay.TrusteePayResp;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoRepayAll;
import com.gofobao.framework.scheduler.biz.TaskSchedulerBiz;
import com.gofobao.framework.scheduler.constants.TaskSchedulerConstants;
import com.gofobao.framework.scheduler.entity.TaskScheduler;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Zeke on 2017/6/1.
 */
@Service
@Slf4j
public class BorrowThirdBizImpl implements BorrowThirdBiz {

    final Gson GSON = new GsonBuilder().create();

    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    private ThirdAccountPasswordHelper thirdAccountPasswordHelper;

    @Autowired
    TaskSchedulerBiz taskSchedulerBiz;
    @Autowired
    AssetChangeProvider assetChangeProvider ;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;


    /**
     * 登记即信标的
     *
     * @param voCreateThirdBorrowReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> createThirdBorrow(VoCreateThirdBorrowReq voCreateThirdBorrowReq) {
        log.error(String.format(String.format("报备标的信息: %s", new Gson().toJson(voCreateThirdBorrowReq))));
        Long borrowId = voCreateThirdBorrowReq.getBorrowId();
        boolean entrustFlag = voCreateThirdBorrowReq.getEntrustFlag();
        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "借款记录不存在！");
        borrow.setReleaseAt(ObjectUtils.isEmpty(borrow.getReleaseAt()) ? new Date() : borrow.getReleaseAt());
        Long userId = borrow.getUserId();
        int repayFashion = borrow.getRepayFashion();

        Long takeUserId = borrow.getTakeUserId();   // 公司实际收款人
        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");
        String productId = StringHelper.toString(borrowId);  // 生成标的的唯一识别码

        DebtRegisterRequest debtRegisterRequest = new DebtRegisterRequest();
        debtRegisterRequest.setAccountId(userThirdAccount.getAccountId());
        debtRegisterRequest.setProductId(productId);
        debtRegisterRequest.setProductDesc(borrow.getName());
        debtRegisterRequest.setRaiseDate(DateHelper.dateToString(borrow.getReleaseAt(), DateHelper.DATE_FORMAT_YMD_NUM));
        debtRegisterRequest.setRaiseEndDate(DateHelper.dateToString(DateHelper.addDays(DateHelper.beginOfDate(borrow.getReleaseAt()), borrow.getValidDay() + 1), DateHelper.DATE_FORMAT_YMD_NUM));
        debtRegisterRequest.setIntType(StringHelper.toString(repayFashion == 1 ? 0 : 1));
        int duration;
        if (repayFashion != 1) {
            Date releaseAt = borrow.getReleaseAt();
            debtRegisterRequest.setIntPayDay(DateHelper.dateToString(releaseAt, "dd"));
            duration = DateHelper.diffInDays(DateHelper.addMonths(releaseAt, borrow.getTimeLimit()), releaseAt, false);
        } else {//一次性还本付息
            duration = borrow.getTimeLimit();
        }
        debtRegisterRequest.setDuration(StringHelper.toString(duration));
        debtRegisterRequest.setTxAmount(StringHelper.formatDouble(borrow.getMoney(), 100, false));
        debtRegisterRequest.setRate(StringHelper.formatDouble(borrow.getApr(), 100, false));
        debtRegisterRequest.setTxFee("0");
        String bailAccountId = jixinHelper.getBailAccountId(borrowId);
        debtRegisterRequest.setBailAccountId(bailAccountId);
        debtRegisterRequest.setAcqRes(StringHelper.toString(borrowId));
        debtRegisterRequest.setChannel(ChannelContant.HTML);
        if (entrustFlag && !ObjectUtils.isEmpty(takeUserThirdAccount)) { //判断是否是受托支付标的
            debtRegisterRequest.setEntrustFlag("1");
            debtRegisterRequest.setReceiptAccountId(takeUserThirdAccount.getAccountId());
        }

        DebtRegisterResponse response = jixinManager.send(JixinTxCodeEnum.DEBT_REGISTER, debtRegisterRequest, DebtRegisterResponse.class);
        if ((ObjectUtils.isEmpty(response))) {
            log.error(String.format(String.format("报备标的信息: 失败 %s", new Gson().toJson(voCreateThirdBorrowReq))));
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        if (!JixinResultContants.SUCCESS.equals(response.getRetCode())) {
            log.error(String.format(String.format("报备标的信息: %s", new Gson().toJson(voCreateThirdBorrowReq))));
            if (response.getRetCode().equals("JX900122")) {  // 查看是否重复登记
                borrow.setProductId(response.getProductId());
            } else {
                log.error(String.format(String.format("报备标的信息: 失败 %s", new Gson().toJson(voCreateThirdBorrowReq))));
                String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
            }
        }
        borrow.setBailAccountId(bailAccountId);
        borrow.setProductId(productId);
        borrowService.updateById(borrow);
        try {
            DebtDetailsQueryRequest debtDetailsQueryRequest = new DebtDetailsQueryRequest();
            debtDetailsQueryRequest.setChannel(ChannelContant.HTML);
            debtDetailsQueryRequest.setAccountId(userThirdAccount.getAccountId());
            debtDetailsQueryRequest.setProductId(productId);
            debtDetailsQueryRequest.setPageSize("10");
            debtDetailsQueryRequest.setPageNum("1");
            DebtDetailsQueryResponse debtDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.DEBT_DETAILS_QUERY,
                    debtDetailsQueryRequest,
                    DebtDetailsQueryResponse.class);
            if ((ObjectUtils.isEmpty(debtDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(debtDetailsQueryResponse.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(debtDetailsQueryResponse) ? "当前网络不稳定，请稍候重试" : debtDetailsQueryResponse.getRetMsg();
                log.error(String.format("查询标的登记情况异常: %s", msg));
                return ResponseEntity.ok(VoBaseResp.ok("创建标的成功!"));
            }
            if (StringUtils.isEmpty(debtDetailsQueryResponse.getSubPacks())) {
                log.error("查询标的登记情况异常: subPacks 为空");
            }
        } catch (Exception e) {
            log.error("查询标的登记情况异常", e);
        }

        log.info(String.format(String.format("报备标的信息: 成功 %s", new Gson().toJson(voCreateThirdBorrowReq))));
        return ResponseEntity.ok(VoBaseResp.ok("创建标的成功!"));
    }

    /**
     * 取消即信借款
     *
     * @param voCancelThirdBorrow
     * @return
     */
    public ResponseEntity<VoBaseResp> cancelThirdBorrow(VoCancelThirdBorrow voCancelThirdBorrow) {
        Long userId = voCancelThirdBorrow.getUserId();
        String productId = voCancelThirdBorrow.getProductId();
        String raiseDate = voCancelThirdBorrow.getRaiseDate();//募集日期
        Preconditions.checkArgument(!StringUtils.isEmpty(raiseDate), "募集日期不能为空!");
        Preconditions.checkArgument(!StringUtils.isEmpty(productId), "当前标的未在即信登记");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");
        DebtRegisterCancelReq request = new DebtRegisterCancelReq();
        request.setChannel(ChannelContant.HTML);
        request.setAccountId(userThirdAccount.getAccountId());
        request.setProductId(productId);
        request.setRaiseDate(raiseDate);
        DebtRegisterCancelResp response = jixinManager.send(JixinTxCodeEnum.DEBT_REGISTER_CANCEL, request, DebtRegisterCancelResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        return ResponseEntity.ok(VoBaseResp.ok("取消借款成功"));
    }

    public DebtDetailsQueryResponse queryThirdBorrowList(VoQueryThirdBorrowList voQueryThirdBorrowList) {
        DebtDetailsQueryResponse debtDetailsQueryResponse = null;
        Long userId = voQueryThirdBorrowList.getUserId();
        String productId = voQueryThirdBorrowList.getProductId();
        String startDate = voQueryThirdBorrowList.getStartDate();
        String endDate = voQueryThirdBorrowList.getEndDate();
        String pageNum = voQueryThirdBorrowList.getPageNum();//页码 从1开始
        String pageSize = voQueryThirdBorrowList.getPageSize();
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        if (ObjectUtils.isEmpty(productId) && (StringUtils.isEmpty(startDate)
                || StringUtils.isEmpty(endDate)) && (StringUtils.isEmpty(pageNum)
                || StringUtils.isEmpty(pageSize))) {
            return debtDetailsQueryResponse;
        }

        DebtDetailsQueryRequest request = new DebtDetailsQueryRequest();
        request.setChannel(ChannelContant.HTML);
        request.setAccountId(userThirdAccount.getAccountId());
        if (!ObjectUtils.isEmpty(productId)) {
            request.setProductId(productId);
        }
        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            request.setEndDate(endDate);
            request.setStartDate(startDate);

        }
        if (!StringUtils.isEmpty(pageNum) && !StringUtils.isEmpty(pageSize)) {
            request.setPageNum(pageNum);
            request.setPageSize(pageSize);
        }

        DebtDetailsQueryResponse response = jixinManager.send(JixinTxCodeEnum.DEBT_DETAILS_QUERY, request, DebtDetailsQueryResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            log.error(msg);
        }

        return response;
    }

    @Override
    public boolean registerBorrrowConditionCheck(Borrow borrow) {
        Long userId = borrow.getUserId();
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "查询标的登记情况异常: 借款人未开户");
        String productId = StringUtils.isEmpty(borrow.getProductId()) ? StringHelper.toString(borrow.getId()) : borrow.getProductId();
        DebtDetailsQueryRequest debtDetailsQueryRequest = new DebtDetailsQueryRequest();
        debtDetailsQueryRequest.setChannel(ChannelContant.HTML);
        debtDetailsQueryRequest.setAccountId(userThirdAccount.getAccountId());
        debtDetailsQueryRequest.setProductId(productId);
        debtDetailsQueryRequest.setPageSize("10");
        debtDetailsQueryRequest.setPageNum("1");
        DebtDetailsQueryResponse debtDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.DEBT_DETAILS_QUERY,
                debtDetailsQueryRequest,
                DebtDetailsQueryResponse.class);
        if ((ObjectUtils.isEmpty(debtDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(debtDetailsQueryResponse.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(debtDetailsQueryResponse) ? "当前网络不稳定，请稍候重试" : debtDetailsQueryResponse.getRetMsg();
            log.error(String.format("查询标的登记情况异常: %s", msg));
            return false;
        }

        String subPacks = debtDetailsQueryResponse.getSubPacks();
        if (StringUtils.isEmpty(subPacks)) {
            log.error("查询标的登记情况异常: subPacks 为空");
            return false;
        }

        List<Map<String, String>> itemList = GSON.fromJson(subPacks, TypeTokenContants.LIST_ALL_STRING_MAP__TOKEN);
        if (CollectionUtils.isEmpty(itemList)) {
            log.error("查询标的登记情况异常: subPacks 为空");
            return false;
        }

        if (itemList.size() > 1) {
            log.error("查询标的登记情况异常: 查询集合超过1个");
            return false;
        }

        String queryProductId = itemList.get(0).get("productId");
        if (StringUtils.isEmpty(borrow.getProductId())) {
            log.info("查询标的登记情况: 保存查询出来的productId");
            borrow.setProductId(queryProductId);
            borrow.setUpdatedAt(new Date());
            borrowService.save(borrow);
        }
        return true;
    }



    /**
     * 即信批次还款(提前结清)
     *
     * @param voRepayAllReq
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdBatchRepayAll(VoRepayAllReq voRepayAllReq) throws Exception {
        Date nowDate = new Date();
        String paramStr = voRepayAllReq.getParamStr();
        if (!SecurityHelper.checkSign(voRepayAllReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }

        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long borrowId = NumberHelper.toLong(paramMap.get("borrowId"));

        Borrow borrow = borrowService.findByIdLock(borrowId);
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());  // 借款人
        Asset borrowAsset = assetService.findByUserId(borrow.getUserId());
        Preconditions.checkNotNull(borrowAsset, "借款人资产记录不存在!");

        //判断提交还款批次是否多次重复提交
        int flag = thirdBatchLogBiz.checkBatchOftenSubmit(String.valueOf(borrowId), ThirdBatchLogContants.BATCH_REPAY);
        if (flag == ThirdBatchLogContants.AWAIT) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("还款处理中，请勿重复点击!")));
        } else if (flag == ThirdBatchLogContants.SUCCESS) {
            //更新状态
        }

        int repaymentTotal = 0;
        List<VoRepayAll> voRepayAllList = new ArrayList<>();
        long penalty = 0;
        int lateInterest = 0;
        int lateDays = 0;
        int overPrincipal = 0;
        Date startAt = null;
        Date endAt = null;
        BorrowRepayment borrowRepayment = null;
        double interestPercent = 0;
        VoRepayAll tempVoRepayAll = null;
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("borrowId", borrowId)
                .build();
        List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);

        for (int i = 0; i < borrowRepaymentList.size(); i++) {
            borrowRepayment = borrowRepaymentList.get(i);
            if (borrowRepayment.getStatus() != 0) {
                continue;
            }

            if (borrowRepayment.getOrder() == 0) {
                startAt = DateHelper.beginOfDate(borrow.getSuccessAt());
            } else {
                startAt = DateHelper.beginOfDate(borrowRepaymentList.get(i - 1).getRepayAt());
            }
            endAt = DateHelper.beginOfDate(borrowRepayment.getRepayAt());

            //以结清第一期的14天利息作为违约金
            if (penalty == 0) { // 违约金
                penalty = borrowRepayment.getInterest() / DateHelper.diffInDays(endAt, startAt, false) * 14;
            }

            Date nowStartDate = DateHelper.beginOfDate(new Date());  // 现在的凌晨时间
            if (nowStartDate.getTime() <= startAt.getTime()) {
                interestPercent = 0;
            } else {
                interestPercent = MathHelper.min(DateHelper.diffInDays(nowStartDate, startAt, false) / DateHelper.diffInDays(endAt, startAt, false), 1);
            }

            lateDays = DateHelper.diffInDays(nowStartDate, endAt, false);
            if (interestPercent == 1 && lateDays > 0) {
                for (int j = i; j < borrowRepaymentList.size(); j++) {
                    overPrincipal += borrowRepaymentList.get(j).getPrincipal();
                }
                lateInterest = new Double(overPrincipal * 0.004 * lateDays).intValue();
            } else {
                lateInterest = 0;
            }
            //累加金额用于判断还款账余额是否充足
            repaymentTotal += borrowRepayment.getPrincipal() + borrowRepayment.getInterest() * interestPercent + lateInterest;
            tempVoRepayAll = new VoRepayAll();
            tempVoRepayAll.setInterestPercent(interestPercent);   // 赔偿利息
            tempVoRepayAll.setRepaymentId(borrowRepayment.getId());
            tempVoRepayAll.setUserId(borrowRepayment.getUserId());
            tempVoRepayAll.setIsUserOpen(false);
            voRepayAllList.add(tempVoRepayAll);
        }

        long repayMoney = repaymentTotal + penalty;
        if (borrowAsset.getUseMoney() < (repayMoney)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "结清总共需要还款 " + repayMoney + " 元，您的账户余额不足，请先充值!！"));
        }

        List<Repay> repayList = new ArrayList<>(); // 往后每期未还回款集合
        for (VoRepayAll tempVoRepayReq : voRepayAllList) {
            //repayList.addAll(borrowRepaymentThirdBiz.getRepayList(tempVoRepayReq));
        }

        if (CollectionUtils.isEmpty(repayList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "还款不存在"));
        }


        //计算总txAmount
        double sumTxAmount = 0;
        double partPenalty = penalty / repayList.size();
        for (Repay tempRepay : repayList) {
            //给每期回款分摊违约金
            tempRepay.setTxFeeOut(StringHelper.formatDouble(NumberHelper.toDouble(tempRepay.getTxFeeOut()) + partPenalty / 100.0, false));
            sumTxAmount += NumberHelper.toDouble(tempRepay.getTxAmount());
        }
        //所有交易利息
        double intAmount = repayList.stream().mapToDouble(r -> NumberHelper.toDouble(r.getIntAmount())).sum();
        //所有还款手续费
        double txFeeOut = repayList.stream().mapToDouble(r -> NumberHelper.toDouble(r.getTxFeeOut())).sum();
        //冻结金额
        double freezeMoney = sumTxAmount + intAmount + txFeeOut;

        //批次号
        String batchNo = jixinHelper.getBatchNo();

        //====================================================================
        //冻结借款人账户资金
        //====================================================================
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(borrowUserThirdAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(freezeMoney, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
            throw new Exception("即信批次还款冻结资金失败：" + balanceFreezeResp.getRetMsg());
        }

        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("borrowId", borrowId);
        acqResMap.put("freezeMoney", freezeMoney);
        acqResMap.put("freezeOrderId", orderId);
        acqResMap.put("userId", borrow.getUserId());

        //立即还款冻结
        long frozenMoney = new Double((freezeMoney) * 100).longValue();
        AssetChange freezeAssetChange = new AssetChange();
        freezeAssetChange.setUserId(borrow.getUserId());
        freezeAssetChange.setType(AssetChangeTypeEnum.freeze);
        freezeAssetChange.setRemark("立即还款冻结可用资金");
        freezeAssetChange.setSeqNo(assetChangeProvider.getSeqNo());
        freezeAssetChange.setMoney(frozenMoney);
        freezeAssetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        freezeAssetChange.setSourceId(borrowRepayment.getId());
        assetChangeProvider.commonAssetChange(freezeAssetChange);;

        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(sumTxAmount, false));
        request.setRetNotifyURL(javaDomain + "/pub/borrow/v2/third/repayall/run");
        request.setNotifyURL(javaDomain + "/pub/borrow/v2/third/repayall/check");
        request.setAcqRes(GSON.toJson(acqResMap));
        request.setSubPacks(GSON.toJson(repayList));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(repayList.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            throw new Exception("即信批次还款失败：" + response.getRetMsg());
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowRepayment.getBorrowId());
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_REPAY_ALL);
        thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
        thirdBatchLog.setRemark("(提前结清)即信批次还款");
        thirdBatchLogService.save(thirdBatchLog);

        return null;
    }


    /**
     * 即信批次还款(提前结清)
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchRepayAllCheckCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchRepayCheckResp repayCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(repayCheckResp)) {
            log.error("=============================(提前结清)即信批次还款检验参数回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(repayCheckResp.getRetCode(), TypeTokenContants.MAP_TOKEN);
        Long borrowId = NumberHelper.toLong(acqResMap.get("borrowId"));
        if (!JixinResultContants.SUCCESS.equals(repayCheckResp.getRetCode())) {
            log.error("=============================(提前结清)即信批次还款检验参数回调===========================");
            log.error("回调失败! msg:" + repayCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), borrowId, 2);
            //
            long userId = NumberHelper.toLong(acqResMap.get("userId"));
            UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(userId);
            String freezeOrderId = StringHelper.toString(acqResMap.get("freezeOrderId"));
            String freezeMoney = StringHelper.toString(acqResMap.get("freezeMoney"));//分

            //解除存管资金冻结
            String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);
            BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
            balanceUnfreezeReq.setAccountId(borrowUserThirdAccount.getAccountId());
            balanceUnfreezeReq.setTxAmount(freezeMoney);
            balanceUnfreezeReq.setChannel(ChannelContant.HTML);
            balanceUnfreezeReq.setOrderId(orderId);
            balanceUnfreezeReq.setOrgOrderId(freezeOrderId);
            BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
            if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
                log.error("===========================================================================");
                log.error("(提前结清)即信批次还款解除冻结资金失败：" + balanceUnfreezeResp.getRetMsg());
                log.error("===========================================================================");
                return ResponseEntity.ok("error");
            }

            //解除本地冻结
            AssetChange assetChange = new AssetChange();
            assetChange.setSourceId(borrowId);
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            assetChange.setMoney(new Double(NumberHelper.toDouble(freezeMoney) * 100).longValue());
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setRemark("(提前结清)即信批次还款解除冻结可用资金");
            assetChange.setType(AssetChangeTypeEnum.unfreeze);
            assetChange.setUserId(userId);
            assetChangeProvider.commonAssetChange(assetChange) ;
        } else {
            log.info("=============================(提前结清)即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), borrowId, 1);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次还款(提前结清)
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchRepayAllRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayRunResp repayRunResp = jixinManager.callback(request, new TypeToken<BatchRepayRunResp>() {
        });
        Map<String, Object> acqResMap = GSON.fromJson(repayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        boolean bool = true;
        if (ObjectUtils.isEmpty(repayRunResp)) {
            log.error("==================================批次回调======================================");
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("请求体为空!");
            log.error("================================================================================");
            log.error("================================================================================");
        }

        if (!JixinResultContants.SUCCESS.equals(repayRunResp.getRetCode())) {
            log.error("==================================批次回调======================================");
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("回调失败! msg:" + repayRunResp.getRetMsg());
            log.error("================================================================================");
            log.error("================================================================================");
        } else {
            log.error("==================================批次回调======================================");
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("回调成功!");
            log.error("================================================================================");
            log.error("================================================================================");
        }

        //触发处理批次放款处理结果队列
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("borrowId")),
                        MqConfig.BATCH_NO, StringHelper.toString(repayRunResp.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq exception", e);
        }

        return ResponseEntity.ok("success");
    }


    /**
     * 即信受托支付
     *
     * @param voThirdTrusteePayReq
     * @param httpServletRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoHtmlResp> thirdTrusteePay(VoThirdTrusteePayReq voThirdTrusteePayReq, HttpServletRequest httpServletRequest) {

        long borrowId = voThirdTrusteePayReq.getBorrowId();
        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "borrowThirdBizImpl thirdTrusteePay： 借款记录不存在!");

        long lendUserId = borrow.getUserId(); //借款人id
        UserThirdAccount lendUserThirdAccount = userThirdAccountService.findByUserId(lendUserId);
        Preconditions.checkNotNull(lendUserThirdAccount, "borrowThirdBizImpl thirdTrusteePay：借款人不存在!");

        long takeUserId = borrow.getTakeUserId(); //收款人id
        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
        Preconditions.checkNotNull(takeUserThirdAccount, "borrowThirdBizImpl thirdTrusteePay：收款人不存在!");

        // 判断是否已经签署受托支付
        TrusteePayQueryReq trusteePayQueryReq = new TrusteePayQueryReq();
        trusteePayQueryReq.setChannel(ChannelContant.HTML);
        trusteePayQueryReq.setProductId(borrow.getProductId());
        trusteePayQueryReq.setAccountId(lendUserThirdAccount.getAccountId());
        TrusteePayQueryResp trusteePayQueryResp = jixinManager.send(JixinTxCodeEnum.TRUSTEE_PAY_QUERY, trusteePayQueryReq, TrusteePayQueryResp.class);
        if (ObjectUtils.isEmpty(trusteePayQueryResp)) {
            log.info(String.format("车贷标/ 渠道标初审: 委托支付状态查询失败( %s )", GSON.toJson(trusteePayQueryResp)));
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }

        if (trusteePayQueryResp.getState().equals("0")) {
            log.info(String.format("车贷标/ 渠道标初审: 委托支付开始( %s )", GSON.toJson(voThirdTrusteePayReq)));
            TrusteePayReq trusteePayReq = new TrusteePayReq();
            trusteePayReq.setAccountId(lendUserThirdAccount.getAccountId());
            trusteePayReq.setChannel(ChannelContant.HTML);
            trusteePayReq.setAcqRes(StringHelper.toString(borrowId));
            trusteePayReq.setIdNo(lendUserThirdAccount.getIdNo());
            trusteePayReq.setIdType(JixinHelper.getIdType(lendUserThirdAccount.getIdType()));
            trusteePayReq.setNotifyUrl(javaDomain + "/pub/borrow/v2/third/trusteepay/run");
            trusteePayReq.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, borrow.getUserId()));   // 忘记密码
            trusteePayReq.setProductId(borrow.getProductId());
            trusteePayReq.setReceiptAccountId(takeUserThirdAccount.getAccountId());
            trusteePayReq.setRetUrl("");
            String html = jixinManager.getHtml(JixinTxCodeEnum.TRUSTEE_PAY, trusteePayReq);

            VoHtmlResp resp = VoBaseResp.ok("请求成功", VoHtmlResp.class);
            try {
                resp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
            } catch (Throwable e) {
                log.error("UserThirdBizImpl autoTender gethtml exceptio", e);
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
            }

            TaskScheduler taskScheduler = new TaskScheduler();
            taskScheduler.setCreateAt(new Date());
            taskScheduler.setUpdateAt(new Date());
            taskScheduler.setType(TaskSchedulerConstants.TRUSTEE_PAY_QUERY);
            Map<String, String> data = new HashMap<>(1);
            data.put("borrowId", String.valueOf(borrowId));
            Gson gson = new Gson();
            taskScheduler.setTaskData(gson.toJson(data));
            taskScheduler.setTaskNum(5);
            taskScheduler = taskSchedulerBiz.save(taskScheduler);
            if (ObjectUtils.isEmpty(taskScheduler.getId())) {
                log.error(String.format("添加查询受托支付调度失败 %s", gson.toJson(data)));
            }

            return ResponseEntity.ok(resp);
        } else {
            if ((JixinResultContants.SUCCESS.equals(trusteePayQueryResp.getRetCode()))) {
                log.info(String.format("车贷标/ 渠道标初审: 委托支付主动触发审核( %s )", GSON.toJson(voThirdTrusteePayReq)));
                MqConfig mqConfig = new MqConfig();
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
                mqConfig.setTag(MqTagEnum.FIRST_VERIFY);
                ImmutableMap<String, String> body = ImmutableMap
                        .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
                mqConfig.setMsg(body);
                log.info(String.format("borrowThirdBizImpl thirdTrusteePay send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "已经申请委托支付, 如有疑问请联系客服!", VoHtmlResp.class));
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
            }
        }


    }

    /**
     * 即信受托支付回调
     *
     * @param request
     * @param response
     */
    public ResponseEntity<String> thirdTrusteePayCall(HttpServletRequest request, HttpServletResponse response) {
        TrusteePayResp trusteePayResp = jixinManager.callback(request, new TypeToken<TrusteePayResp>() {
        });
        if (ObjectUtils.isEmpty(trusteePayResp)) {
            log.error("BorrowThirdBizImpl thirdTrusteePayCall 参数有问题");
            return ResponseEntity.ok("success");
        }

        if (!JixinResultContants.SUCCESS.equals(trusteePayResp.getRetCode())) {
            log.error(String.format("BorrowThirdBizImpl thirdTrusteePayCall 受托支付回调失败: %s", trusteePayResp.getRetMsg()));
            return ResponseEntity.ok("success");
        }
        Long borrowId = NumberHelper.toLong(trusteePayResp.getAcqRes());
        if (trusteePayResp.getState().equals("1")) {   // 初审通过
            //初审
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
            mqConfig.setTag(MqTagEnum.FIRST_VERIFY);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            log.info(String.format("borrowThirdBizImpl firstVerify send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } else { // 等待查询
            TaskScheduler taskScheduler = new TaskScheduler();
            taskScheduler.setCreateAt(new Date());
            taskScheduler.setUpdateAt(new Date());
            taskScheduler.setType(TaskSchedulerConstants.TRUSTEE_PAY_QUERY);
            Map<String, String> data = new HashMap<>(1);
            data.put("borrowId", borrowId.toString());
            Gson gson = new Gson();
            taskScheduler.setTaskData(gson.toJson(data));
            taskScheduler.setTaskNum(Integer.MAX_VALUE - 2);
            taskScheduler = taskSchedulerBiz.save(taskScheduler);
            if (ObjectUtils.isEmpty(taskScheduler.getId())) {
                log.error(String.format("添加查询受托支付调度失败 %s", gson.toJson(data)));
            }

        }

        return ResponseEntity.ok("success");
    }
}
