package com.gofobao.framework.listener.providers;

import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.fund_trans_query.FundTransQueryRequest;
import com.gofobao.framework.api.model.fund_trans_query.FundTransQueryResponse;
import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.CashDetailLogService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.biz.IncrStatisticBiz;
import com.gofobao.framework.system.biz.NoticesBiz;
import com.gofobao.framework.system.entity.IncrStatistic;
import com.gofobao.framework.system.entity.Notices;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Map;

import static com.gofobao.framework.helper.RechargeAndCashAndRedpackQueryHelper.QueryType.QUERY_CASH;
import static com.gofobao.framework.helper.RechargeAndCashAndRedpackQueryHelper.QueryType.QUERY_RECHARGE;

/**
 * Created by Max on 17/6/1.
 */
@Component
@Slf4j
public class UserActiveProvider {
    @Autowired
    JixinManager jixinManager;

    @Autowired
    UserThirdBiz userThirdBiz;

    @Autowired
    IncrStatisticBiz incrStatisticBiz;

    @Autowired
    RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    NoticesBiz noticesBiz;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    UserService userService;

    @Autowired
    CashDetailLogService cashDetailLogService;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    MqHelper mqHelper;


    /**
     * 注册活动
     *
     * @param body
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean registerActive(Map<String, String> body) throws Exception {
        Long userId = Long.parseLong(body.get(MqConfig.MSG_USER_ID));
        // 赠送用户体验金
        //awardVirtualMoney(userId, 1000);
        // 增加统计
        IncrStatistic incrStatistic = new IncrStatistic();
        incrStatistic.setRegisterCount(1);
        incrStatistic.setRegisterTotalCount(1);
        incrStatisticBiz.caculate(incrStatistic);
        return true;
    }

    /**
     * 赠送体验金
     *
     * @param userId
     * @param money
     * @throws Exception
     */
    private void awardVirtualMoney(Long userId, Integer money) throws Exception {
        log.info(String.format("award VirtualMoney: %s", userId));
        //赠送体验金
        AssetChange assetChange = new AssetChange();
        assetChange.setType(AssetChangeTypeEnum.unfreeze);  // 招标失败解除冻结资金
        assetChange.setUserId(userId);
        assetChange.setMoney(money * 100);
        assetChange.setRemark("赠送体验金");
        assetChange.setSourceId(userId);
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChangeProvider.commonAssetChange(assetChange);
        log.info("award virtualMoney success");
    }


    /**
     * 充值成功
     *
     * @param msg
     * @return
     */
    public boolean recharge(Map<String, String> msg) {
        Long rechargeId = Long.parseLong(msg.get(MqConfig.MSG_ID));
        if (ObjectUtils.isEmpty(rechargeId)) {
            log.error("充值MQ回调参数为空");
            return false;
        }


        RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findById(rechargeId);
        if (ObjectUtils.isEmpty(rechargeDetailLog)) {
            log.error("查无充值记录");
            return false;
        }

        // 充值发送通知
        String name = String.format("充值%s元已成功", StringHelper.formatDouble(rechargeDetailLog.getMoney() / 100D, true));
        String content = String.format("您已经于%s成功充值%s元", DateHelper.dateToString(rechargeDetailLog.getCreateTime()), StringHelper.formatDouble(rechargeDetailLog.getMoney() / 100D, true));
        Notices notices = new Notices();
        notices.setRead(false);
        notices.setCreatedAt(new Date());
        notices.setUserId(rechargeDetailLog.getUserId());
        notices.setFromUserId(0L);
        notices.setContent(content);
        notices.setName(name);
        notices.setType("system");
        noticesBiz.save(notices);
        return true;
    }


    /**
     * 用户登录事件触发
     *
     * @param msg
     * @return
     */
    public boolean userLogin(Map<String, String> msg) {
        Long userId = Long.parseLong(msg.get(MqConfig.MSG_USER_ID));
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return true;
        }

        //===================
        // 同步签约状态
        //===================
        userThirdBiz.synCreditQuth(userThirdAccount);

        //=================
        // 同步提现
        //=================


        //=================
        // 同步充值
        //=================


        return true;
    }


    /**
     * 即信单笔资金操作确认
     *
     * @param msg
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean jixinOpQuery(Map<String, String> msg) throws Exception {
        String sourceId = msg.get(MqConfig.SOURCE_ID);
        String userId = msg.get(MqConfig.MSG_USER_ID);
        String type = msg.get(MqConfig.TYPE);
        Gson gson = new Gson();
        Users user = userService.findByIdLock(Long.parseLong(userId));
        Preconditions.checkNotNull(user, "即信单笔确认用户为空");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(Long.parseLong(userId));
        Preconditions.checkNotNull(userThirdAccount, "即信单笔确认用户为空");
        boolean result = false;
        if (type.equalsIgnoreCase(QUERY_CASH.getValue())) {
            log.info("提现充值确认");
            result = doCashOpQuery(Long.parseLong(sourceId), userThirdAccount);
        } else if (type.equalsIgnoreCase(QUERY_RECHARGE.getValue())) {
            log.info("充值确认");
            result = doRechargeOpQuery(Long.parseLong(sourceId), userThirdAccount);
        }

        if(result){
            log.info(String.format("单笔资金操作处理成功处理成功:%s", gson.toJson(msg)));
        }else{
            log.error(String.format("单笔资金操作处理成功处理失败:%s", gson.toJson(msg)));
        }

        return false;
    }

    /**
     * 充值主动查询
     *
     * @param sourceId
     * @param userThirdAccount
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doRechargeOpQuery(long sourceId, UserThirdAccount userThirdAccount) throws Exception {
        RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findById(sourceId);
        Preconditions.checkNotNull(rechargeDetailLog, "UserActiveProvider.doRechargeOpQuery rechargeDetailLog record is null");
        if (rechargeDetailLog.getState() == 1) {
            log.info("充值单笔金额查询, 重复调用");
            return true;
        }

        Gson gson = new Gson();
        Date nowDate = new Date();
        String seqNo = rechargeDetailLog.getSeqNo();
        FundTransQueryResponse fundTransQueryResponse = queryJixinOp(seqNo, userThirdAccount);
        if (ObjectUtils.isEmpty(fundTransQueryResponse)) {
            exceptionEmailHelper.sendErrorMessage("充值单笔资金操作确认请求异常", String.format("充值Id: %s", sourceId));
            return false;
        }

        if ("00".equalsIgnoreCase(fundTransQueryResponse.getResult())) {  // 进行增加用户资金操作
            AssetChange entity = new AssetChange();
            String groupSeqNo = assetChangeProvider.getGroupSeqNo();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(rechargeDetailLog.getMoney());
            entity.setSeqNo(seqNo);
            entity.setUserId(rechargeDetailLog.getUserId());
            entity.setRemark(String.format("你在 %s 成功充值%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(rechargeDetailLog.getMoney() / 100D, false)));
            entity.setType(AssetChangeTypeEnum.onlineRecharge);
            assetChangeProvider.commonAssetChange(entity);

            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 10));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
        } else {  // 失败发送用户, 通知
            exceptionEmailHelper.sendErrorMessage("充值单笔资金查询失败!",  String.format("充值Id: %s", sourceId));
            String titel = "充值失败";
            String content = String.format("敬爱的用户您好! 你在[%s]充值%s元, 存管系统处理失败!", DateHelper.dateToString(rechargeDetailLog.getCreateTime()),
                    StringHelper.formatDouble(rechargeDetailLog.getMoney() / 100D, true));
            try {
                Notices notices = new Notices();
                notices.setFromUserId(1L);
                notices.setUserId(userThirdAccount.getUserId());
                notices.setRead(false);
                notices.setName(titel);
                notices.setContent(content);
                notices.setType("system");
                notices.setCreatedAt(nowDate);
                notices.setUpdatedAt(nowDate);
                MqConfig mqConfig = new MqConfig();
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
                mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
                Map<String, String> body = gson.fromJson(gson.toJson(notices), TypeTokenContants.MAP_TOKEN);
                mqConfig.setMsg(body);
                log.info(String.format("UserActiveProvider doRechargeOpQuery send mq %s", gson.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("UserActiveProvider doRechargeOpQuery send mq exception", e);
            }
        }

        return true;
    }


    /**
     * 查询用户操作
     *
     * @param seqNo
     * @param userThirdAccount
     * @return
     */
    private FundTransQueryResponse queryJixinOp(String seqNo, UserThirdAccount userThirdAccount) {
        String orgTxDate = seqNo.substring(0, 8);  // 原始日期
        String orgTxTime = seqNo.substring(8, 14);  // 原始时间
        String orgSeqNo = seqNo.substring(14);  // 原始序列号

        FundTransQueryRequest fundTransQueryRequest = new FundTransQueryRequest();
        fundTransQueryRequest.setAccountId(userThirdAccount.getAccountId());
        fundTransQueryRequest.setOrgSeqNo(orgSeqNo);
        fundTransQueryRequest.setOrgTxDate(orgTxDate);
        fundTransQueryRequest.setOrgTxTime(orgTxTime);
        FundTransQueryResponse fundTransQueryResponse = jixinManager.send(JixinTxCodeEnum.FUND_TRANS_QUERY,
                fundTransQueryRequest,
                FundTransQueryResponse.class);

        if (ObjectUtils.isEmpty(fundTransQueryResponse)
                || !JixinResultContants.SUCCESS.equalsIgnoreCase(fundTransQueryResponse.getTxCode())) {
            try {
                Thread.sleep(2 * 1000L);
            } catch (InterruptedException e) {
                log.info("单笔资金操作确认休眠异常");
            }
            fundTransQueryResponse = jixinManager.send(JixinTxCodeEnum.FUND_TRANS_QUERY,
                    fundTransQueryRequest,
                    FundTransQueryResponse.class);
            if (ObjectUtils.isEmpty(fundTransQueryResponse)
                    || !JixinResultContants.SUCCESS.equalsIgnoreCase(fundTransQueryResponse.getTxCode())) {
                return null;
            }
        }
        return fundTransQueryResponse;
    }

    /**
     * 提现主动查询
     *
     * @param sourceId         提现记录ID
     * @param userThirdAccount
     * @return
     */
    private boolean doCashOpQuery(long sourceId, UserThirdAccount userThirdAccount) throws Exception {
        CashDetailLog cashDetailLog = cashDetailLogService.findById(sourceId);
        Preconditions.checkNotNull(cashDetailLog, "UserActiveProvider.doCashOpQuery cashDetailLog record is null");
        Gson gson = new Gson();
        Date nowDate = new Date();
        String seqNo = cashDetailLog.getSeqNo();

        FundTransQueryResponse fundTransQueryResponse = queryJixinOp(seqNo, userThirdAccount);
        if (ObjectUtils.isEmpty(fundTransQueryResponse)) {
            exceptionEmailHelper.sendErrorMessage("提现单笔资金操作确认请求异常", String.format("提现Id: %s", sourceId));
            return false;
        }

        String titel = null;
        String content = null;
        // 查询操作是否成功
        if ("00".equalsIgnoreCase(fundTransQueryResponse.getResult())) {
            titel = "提现成功";
            content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 已经处理成功!.", DateHelper.dateToString(cashDetailLog.getCreateTime()),
                    StringHelper.formatDouble(cashDetailLog.getMoney() / 100D, true));
        } else { // 失败进行撤销
            try {
                exceptionEmailHelper.sendErrorMessage("提现失败, 发生资金撤回", String.format("提现Id: %s", sourceId));
                cashDetailLog.setState(4);  // 更改用户提现记录
                cashDetailLog.setCallbackTime(nowDate);
                cashDetailLogService.save(cashDetailLog);
                long userId = cashDetailLog.getUserId();

                // 更改用户资金
                AssetChange entity = new AssetChange();
                long realCashMoney = cashDetailLog.getMoney() - cashDetailLog.getFee();
                String groupSeqNo = assetChangeProvider.getGroupSeqNo();
                entity.setGroupSeqNo(groupSeqNo);
                entity.setMoney(realCashMoney);
                entity.setSeqNo(seqNo);
                entity.setUserId(userId);
                entity.setRemark(String.format("你在 %s 成功返还提现%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(realCashMoney / 100D, true)));
                entity.setType(AssetChangeTypeEnum.cancelBigCash);
                assetChangeProvider.commonAssetChange(entity);
                if (cashDetailLog.getFee() > 0) {   // 扣除用户提现手续费
                    Long feeAccountId = assetChangeProvider.getFeeAccountId();
                    entity = new AssetChange();
                    entity.setGroupSeqNo(groupSeqNo);
                    entity.setMoney(cashDetailLog.getFee());
                    entity.setSeqNo(seqNo);
                    entity.setUserId(userId);
                    entity.setForUserId(feeAccountId);
                    entity.setRemark(String.format("你在 %s 成功返还提现手续费%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true)));
                    entity.setType(AssetChangeTypeEnum.cancelBigCashFee);
                    assetChangeProvider.commonAssetChange(entity);

                    // 平台收取提现手续费
                    entity = new AssetChange();
                    entity.setGroupSeqNo(groupSeqNo);
                    entity.setMoney(cashDetailLog.getFee());
                    entity.setSeqNo(seqNo);
                    entity.setUserId(feeAccountId);
                    entity.setForUserId(userId);
                    entity.setRemark(String.format("你在 %s 成功返还提现手续费%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true)));
                    entity.setType(AssetChangeTypeEnum.cancelPlatformBigCashFee);
                    assetChangeProvider.commonAssetChange(entity);
                }

                titel = "提现失败";
                content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 由于已被银行拒绝受理, 现在归还提现资金. 如有疑问联系平台客服!", DateHelper.dateToString(cashDetailLog.getCreateTime()),
                        StringHelper.formatDouble(cashDetailLog.getMoney() / 100D, true));
            } catch (Exception e) {
                exceptionEmailHelper.sendException("提现资金拨正错误", e);
                throw new Exception(e);
            }
        }

        try {
            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(userThirdAccount.getUserId());
            notices.setRead(false);
            notices.setName(titel);
            notices.setContent(content);
            notices.setType("system");
            notices.setCreatedAt(nowDate);
            notices.setUpdatedAt(nowDate);
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = gson.fromJson(gson.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            log.info(String.format("UserActiveProvider doCashOpQuery send mq %s", gson.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("UserActiveProvider doCashOpQuery send mq exception", e);
        }

        return true;
    }
}
