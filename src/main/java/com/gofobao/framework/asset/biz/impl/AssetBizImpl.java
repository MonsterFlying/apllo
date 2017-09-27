package com.gofobao.framework.asset.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.*;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.direct_recharge_online.DirectRechargeOnlineRequest;
import com.gofobao.framework.api.model.direct_recharge_online.DirectRechargeOnlineResponse;
import com.gofobao.framework.api.model.direct_recharge_plus.DirectRechargePlusRequest;
import com.gofobao.framework.api.model.direct_recharge_plus.DirectRechargePlusResponse;
import com.gofobao.framework.api.model.offline_recharge_call.OfflineRechargeCallRequest;
import com.gofobao.framework.api.model.offline_recharge_call.OfflineRechargeCallResponse;
import com.gofobao.framework.api.model.offline_recharge_call.OfflineRechargeCallbackResponse;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelRequest;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelResponse;
import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.biz.AssetSynBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.*;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.asset.vo.response.pc.AssetLogs;
import com.gofobao.framework.asset.vo.response.pc.VoViewAssetLogsWarpRes;
import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.jxl.ExcelException;
import com.gofobao.framework.common.jxl.ExcelUtil;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.member.vo.response.pc.AssetStatistic;
import com.gofobao.framework.member.vo.response.pc.ExpenditureDetail;
import com.gofobao.framework.member.vo.response.pc.IncomeEarnedDetail;
import com.gofobao.framework.member.vo.response.pc.VoViewAssetStatisticWarpRes;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.tender.vo.request.VoAdminRechargeReq;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
@Slf4j
public class AssetBizImpl implements AssetBiz {

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    RechargeAndCashAndRedpackQueryHelper rechargeAndCashAndRedpackQueryHelper;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    AssetService assetService;
    @Autowired
    private JixinHelper jixinHelper;

    @Autowired
    AssetLogService assetLogService;

    @Autowired
    UserCacheService userCacheService;

    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    RedisHelper redisHelper;

    @Value("${gofobao.javaDomain}")
    String javaDomain;

    @Value("${gofobao.h5Domain}")
    String h5Domain;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    AssetSynBiz assetSynBiz;

    @Autowired
    MqHelper mqHelper;

    @Autowired
    DictValueService dictValueServcie;

    @Autowired
    DictItemService dictItemService;

    @Autowired
    BankAccountBizImpl bankAccountBiz;

    @Autowired
    AssetChangeProvider assetChangeProvider;


    @Autowired
    MarketingRedpackRecordService marketingRedpackRecordService;
    LoadingCache<String, DictValue> bankLimitCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemService.findTopByAliasCodeAndDel("PLATFORM_BANK", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    return dictValueServcie.findTopByItemIdAndValue02(dictItem.getId(), bankName);
                }
            });

    private final Gson GSON = new Gson();

    @Autowired
    NewAssetLogService newAssetLogService;

    /**
     * 撤回即信红包
     *
     * @param voUnsendRedPacket
     * @return
     */
    public ResponseEntity<VoBaseResp> unsendRedPacket(VoUnsendRedPacket voUnsendRedPacket) {
        String paramStr = voUnsendRedPacket.getParamStr();/* pc请求提前结清参数 */
        if (!SecurityHelper.checkSign(voUnsendRedPacket.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        long redpackAccountId = 0;
        try {
            redpackAccountId = assetChangeProvider.getRedpackAccountId();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redpackAccountId);
        /* 红包发送编号 */
        String sendSeqNo = paramMap.get("sendSeqNo");
        Double money = NumberHelper.toDouble(paramMap.get("money"));
        long userId = NumberHelper.toLong(paramMap.get("userId"));
        String dateStr = paramMap.get("dateStr");
        String timeStr = paramMap.get("timeStr");

        /* 存管账户记录 */
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "存管账户记录为空!");

        VoucherPayCancelRequest voucherPayCancelRequest = new VoucherPayCancelRequest();
        voucherPayCancelRequest.setAccountId(redpackAccount.getAccountId());
        voucherPayCancelRequest.setTxAmount(StringHelper.formatDouble(money, false));
        voucherPayCancelRequest.setOrgTxDate(dateStr);
        voucherPayCancelRequest.setOrgTxTime(timeStr);
        voucherPayCancelRequest.setForAccountId(userThirdAccount.getAccountId());
        voucherPayCancelRequest.setOrgSeqNo(sendSeqNo);
        voucherPayCancelRequest.setAcqRes(String.valueOf(userId));
        voucherPayCancelRequest.setChannel(ChannelContant.HTML);
        VoucherPayCancelResponse response = jixinManager.send(JixinTxCodeEnum.UNSEND_RED_PACKET, voucherPayCancelRequest, VoucherPayCancelResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络出现异常, 请稍后尝试！" : response.getRetMsg();
            log.error(msg);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, String.format("撤销红包失败：%s", response.getRetMsg())));
        }
        return ResponseEntity.ok(VoBaseResp.ok(String.format("撤销红包成功：msg->%s", response.getRetMsg())));
    }


    /**
     * 发送即信红包
     *
     * @param voSendRedPacket
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> sendRedPacket(VoSendRedPacket voSendRedPacket) {
        String paramStr = voSendRedPacket.getParamStr();/* pc请求提前结清参数 */
        if (!SecurityHelper.checkSign(voSendRedPacket.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc发送红包 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        long redpackAccountId = 0;
        try {
            redpackAccountId = assetChangeProvider.getRedpackAccountId();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redpackAccountId);
        //发送红包参数
        long userId = NumberHelper.toLong(paramMap.get("userId"));
        /* 用户记录*/
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "用户记录不存在!");
        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "用户资产记录不存在!");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "存管账户记录不存在!");
        /* 红包发放金额 */
        double money = MoneyHelper.multiply(NumberHelper.toDouble(paramMap.get("money")), 100d);
        if (money <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc发送红包 发放金额为空!"));
        }

        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc发送红包 用户已锁定!"));
        }

        //3.发送红包
        VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
        voucherPayRequest.setAccountId(redpackAccount.getAccountId());
        voucherPayRequest.setTxAmount(StringHelper.formatDouble(asset.getUseMoney(), 100, false));
        voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
        voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
        voucherPayRequest.setDesLine("红包发送!");
        voucherPayRequest.setChannel(ChannelContant.HTML);
        VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("pc发送红包 请求即信异常:%s", msg)));
        }

        return ResponseEntity.ok(VoBaseResp.ok("发送成功!"));
    }

    /**
     * 获取用户资产详情
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoUserAssetInfoResp> userAssetInfo(Long userId) {
        Asset asset = assetService.findByUserId(userId); //查询会员资产信息
        if (ObjectUtils.isEmpty(asset)) {
            return null;
        }

        UserCache userCache = userCacheService.findById(userId);  //查询会员缓存信息
        if (ObjectUtils.isEmpty(userCache)) {
            return null;
        }

        Long useMoney = asset.getUseMoney();
        Long payment = asset.getPayment();
        long netWorthQuota = new Double((useMoney + userCache.getWaitCollectionPrincipal()) * 0.8 - payment).longValue();//计算净值额度
        Long netAsset = new Double((asset.getCollection() + asset.getNoUseMoney() + asset.getUseMoney()) - asset.getPayment()).longValue();
        VoUserAssetInfoResp voUserAssetInfoResp = VoBaseResp.ok("成功", VoUserAssetInfoResp.class);
        voUserAssetInfoResp.setHideUserMoney(StringHelper.formatDouble(useMoney / 100D, true));
        voUserAssetInfoResp.setHideNoUseMoney(StringHelper.formatDouble(asset.getNoUseMoney() / 100D, true));
        voUserAssetInfoResp.setHidePayment(StringHelper.formatDouble(payment / 100D, true));
        voUserAssetInfoResp.setHideCollection(StringHelper.formatDouble(asset.getCollection() / 100D, true));
        voUserAssetInfoResp.setHideVirtualMoney(StringHelper.formatDouble(asset.getVirtualMoney() / 100D, true));
        voUserAssetInfoResp.setHideNetWorthQuota(StringHelper.formatDouble((netWorthQuota > 0 ? netWorthQuota : 0) / 100D, true));
        voUserAssetInfoResp.setUseMoney(useMoney);
        voUserAssetInfoResp.setNoUseMoney(asset.getNoUseMoney());
        voUserAssetInfoResp.setPayment(asset.getPayment());
        voUserAssetInfoResp.setCollection(asset.getCollection());
        voUserAssetInfoResp.setVirtualMoney(asset.getVirtualMoney());
        voUserAssetInfoResp.setNetWorthQuota(netWorthQuota > 0 ? netWorthQuota : 0);
        voUserAssetInfoResp.setNetAsset(StringHelper.formatMon(netAsset / 100D));
        voUserAssetInfoResp.setIncomeTotal(StringHelper.formatMon(userCache.getIncomeTotal() / 100D));
        voUserAssetInfoResp.setHideIncomeTotal(userCache.getIncomeTotal());
        return ResponseEntity.ok(voUserAssetInfoResp);
    }

    /**
     * 账户流水
     *
     * @return
     */
    @Override
    public ResponseEntity<VoViewAssetLogWarpRes> assetLogResList(VoAssetLogReq voAssetLogReq) {
        try {
            List<VoViewAssetLogRes> resList = assetLogService.assetLogList(voAssetLogReq);
            VoViewAssetLogWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewAssetLogWarpRes.class);
            warpRes.setResList(resList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询成功", VoViewAssetLogWarpRes.class));
        }
    }

    /**
     * pc: 资金流水
     *
     * @param voAssetLogReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewAssetLogsWarpRes> pcAssetLogs(VoAssetLogReq voAssetLogReq) {
        try {
            VoViewAssetLogsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewAssetLogsWarpRes.class);
            List<AssetLogs> resList = assetLogService.pcAssetLogs(voAssetLogReq);
            warpRes.setTotalCount(0);
            if (!CollectionUtils.isEmpty(resList)) {
                warpRes.setTotalCount(resList.get(0).getTotalCount());
                resList.get(0).setTotalCount(null);
            }
            warpRes.setAssetLogs(resList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败,请稍后再试", VoViewAssetLogsWarpRes.class));
        }
    }

    @Override
    public String rechargeShow(HttpServletRequest request, Model model, String seqNo) {
        // 查询充值信息
        RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findTopBySeqNo(seqNo);
        model.addAttribute("h5Domain", h5Domain);
        if (ObjectUtils.isEmpty(rechargeDetailLog)) {
            return "recharge/faile";
        } else if (rechargeDetailLog.getState() == 0) {
            return "recharge/loading";
        } else if (rechargeDetailLog.getState() == 1) {
            return "recharge/success";
        } else {
            return "recharge/faile";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> rechargeOnline(HttpServletRequest request, VoRechargeReq voRechargeReq) throws Exception {
        Users users = userService.findByIdLock(voRechargeReq.getUserId());
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！"));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voRechargeReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！"));
        }
        if (StringUtils.isEmpty(userThirdAccount.getCardNo())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_BIND_BANK_CARD, "对不起!你的账号还没绑定银行卡呢"));
        }
        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！"));
        }

        String smsSeq = null;
        try {
            smsSeq = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.DIRECT_RECHARGE_ONLINE, voRechargeReq.getPhone()), null);
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.DIRECT_RECHARGE_ONLINE, userThirdAccount.getMobile()));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl rechargeOnline get redis exception ", e);
        }

        if (StringUtils.isEmpty(smsSeq)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取"));
        }
        // 充值额度
        double[] rechargeCredit = bankAccountBiz.getRechargeCredit(voRechargeReq.getUserId());
        // 判断单笔额度
        double oneTimes = rechargeCredit[0];
        if (voRechargeReq.getMoney() > oneTimes) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("%s每笔最大充值额度为%s元",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(oneTimes, true))));
        }

        // 判断当天额度
        double dayTimes = rechargeCredit[1];
        if ((dayTimes <= 0) || (dayTimes - voRechargeReq.getMoney() < 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("今天你在%s的剩余充值额度%s",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(dayTimes < 0 ? 0 : dayTimes, true))));
        }

        /**
         // 判断每月额度
         double mouthTimes = rechargeCredit[2];
         if ((mouthTimes <= 0) || (mouthTimes - voRechargeReq.getMoney() < 0)) {
         return ResponseEntity
         .badRequest()
         .body(VoBaseResp.error(VoBaseResp.ERROR,
         String.format("当月你在%s的剩余充值额度%s元",
         userThirdAccount.getBankName(),
         StringHelper.formatDouble(mouthTimes < 0 ? 0 : mouthTimes, true))));
         }*/

        DirectRechargeOnlineRequest directRechargeOnlineRequest = new DirectRechargeOnlineRequest();
        directRechargeOnlineRequest.setSeqNo(RandomHelper.generateNumberCode(6));
        directRechargeOnlineRequest.setTxTime(DateHelper.getTime());
        directRechargeOnlineRequest.setTxDate(DateHelper.getDate());
        directRechargeOnlineRequest.setAccountId(userThirdAccount.getAccountId());
        directRechargeOnlineRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        directRechargeOnlineRequest.setIdNo(userThirdAccount.getIdNo());
        directRechargeOnlineRequest.setName(userThirdAccount.getName());
        directRechargeOnlineRequest.setMobile(voRechargeReq.getPhone());
        directRechargeOnlineRequest.setCardNo(userThirdAccount.getCardNo());
        directRechargeOnlineRequest.setCurrency("156");
        directRechargeOnlineRequest.setSmsSeq(smsSeq);
        directRechargeOnlineRequest.setSmsCode(voRechargeReq.getSmsCode());
        directRechargeOnlineRequest.setAcqRes(users.getId().toString());
        directRechargeOnlineRequest.setChannel(ChannelContant.getchannel(request));
        directRechargeOnlineRequest.setTxAmount(voRechargeReq.getMoney().toString());
        DirectRechargeOnlineResponse directRechargeOnlineResponse = jixinManager.send(JixinTxCodeEnum.DIRECT_RECHARGE_ONLINE, directRechargeOnlineRequest, DirectRechargeOnlineResponse.class);
        if (ObjectUtils.isEmpty(directRechargeOnlineResponse)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不稳定, 请稍后重试!"));
        }
        Gson gson = new Gson();
        int state;
        String msg = "";
        Date now = new Date();
        boolean toBeConform = false;  // 是否待查询
        if (directRechargeOnlineResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {  // 充值成功
            log.info(String.format("充值成功: %s", gson.toJson(directRechargeOnlineResponse)));
            state = 1;
            msg = directRechargeOnlineResponse.getRetMsg();
            AssetChange entity = new AssetChange();
            String groupSeqNo = assetChangeProvider.getGroupSeqNo();
            String seqNo = String.format("%s%s%s", directRechargeOnlineResponse.getTxDate(), directRechargeOnlineResponse.getTxTime(), directRechargeOnlineResponse.getSeqNo());
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(new Double(voRechargeReq.getMoney() * 100).longValue());
            entity.setSeqNo(seqNo);
            entity.setUserId(users.getId());
            entity.setRemark(String.format("你在 %s 成功充值%s元", DateHelper.dateToString(now), voRechargeReq.getMoney()));
            entity.setType(AssetChangeTypeEnum.onlineRecharge);
            assetChangeProvider.commonAssetChange(entity);
        } else if (JixinResultContants.toBeConfirm(directRechargeOnlineResponse)) { // 需要确认
            log.info(String.format("充值待确认: %s", gson.toJson(directRechargeOnlineResponse)));
            state = 2; // 充值失败
            msg = "非常抱歉, 当前充值状态不明确, 系统需要在10分钟后确认是否充值成功!";
            toBeConform = true;
        } else {
            log.error(String.format("请求即信联机充值异常: %s", gson.toJson(directRechargeOnlineResponse)));
            state = 2;
            msg = directRechargeOnlineResponse.getRetMsg();
        }

        // 插入充值记录
        RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
        rechargeDetailLog.setUserId(users.getId());
        rechargeDetailLog.setBankName(userThirdAccount.getBankName());
        rechargeDetailLog.setCallbackTime(null);
        rechargeDetailLog.setCreateTime(now);
        rechargeDetailLog.setUpdateTime(now);
        rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
        rechargeDetailLog.setDel(0);
        rechargeDetailLog.setIp(IpHelper.getIpAddress(request));
        rechargeDetailLog.setMobile(voRechargeReq.getPhone());
        Double recordRecharge = MoneyHelper.multiply(voRechargeReq.getMoney(), 100D, 0);
        rechargeDetailLog.setMoney(recordRecharge.longValue());
        rechargeDetailLog.setRechargeChannel(0);
        rechargeDetailLog.setState(state); // 充值成功
        rechargeDetailLog.setSeqNo(directRechargeOnlineRequest.getTxDate() + directRechargeOnlineRequest.getTxTime() + directRechargeOnlineRequest.getSeqNo());
        rechargeDetailLog.setResponseMessage(gson.toJson(directRechargeOnlineResponse));  // 响应吗
        RechargeDetailLog saveRechargeDetailLog = rechargeDetailLogService.save(rechargeDetailLog);

        // 触发发送短信
        if (toBeConform) {
            rechargeAndCashAndRedpackQueryHelper.save(RechargeAndCashAndRedpackQueryHelper.QueryType.QUERY_RECHARGE, users.getId(), saveRechargeDetailLog.getId(), false);
        }

        if (state == 1) {
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(now, 10));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
            log.info("触发充值记录调度");
            return ResponseEntity.ok(VoBaseResp.ok("充值成功"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("充值失败, %s", msg)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoUserAssetInfoResp> synOffLineRecharge(Long userId) throws Exception {
        try {
            assetSynBiz.doAssetSyn(userId);
        } catch (Exception e) {
            log.error("资金同步异常", e);
        }
        return userAssetInfo(userId);
    }

    @Override
    public ResponseEntity<VoHtmlResp> recharge(HttpServletRequest request, VoRechargeReq voRechargeReq) {
        Users users = userService.findById(voRechargeReq.getUserId());
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoHtmlResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voRechargeReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoHtmlResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoHtmlResp.class));
        }


        String srvTxCode = null;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.DIRECT_RECHARGE_PLUS, userThirdAccount.getMobile()), null);
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.DIRECT_RECHARGE_PLUS, userThirdAccount.getMobile()));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl autoTender get redis exception ", e);
        }

        if (StringUtils.isEmpty(srvTxCode)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取", VoHtmlResp.class));
        }

        // 提现额度判断
        // 判断提现额度剩余
        double[] rechargeCredit = bankAccountBiz.getRechargeCredit(voRechargeReq.getUserId());
        // 判断单笔额度
        double oneTimes = rechargeCredit[0];
        if (voRechargeReq.getMoney() > oneTimes) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("%s每笔最大充值额度为%s元",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(oneTimes, true)),
                            VoHtmlResp.class));
        }

        // 判断当天额度
        double dayTimes = rechargeCredit[1];
        if ((dayTimes <= 0) || (dayTimes - voRechargeReq.getMoney() < 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("今天你在%s的剩余充值额度%s",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(dayTimes < 0 ? 0 : dayTimes, true)),
                            VoHtmlResp.class));
        }

        // 判断每月额度
        double mouthTimes = rechargeCredit[2];
        if ((mouthTimes <= 0) || (mouthTimes - voRechargeReq.getMoney() < 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("当月你在%s的剩余充值额度%s元",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(mouthTimes < 0 ? 0 : mouthTimes, true)),
                            VoHtmlResp.class));
        }

        DirectRechargePlusRequest directRechargePlusRequest = new DirectRechargePlusRequest();
        directRechargePlusRequest.setSeqNo(RandomHelper.generateNumberCode(6));
        directRechargePlusRequest.setTxTime(DateHelper.getTime());
        directRechargePlusRequest.setTxDate(DateHelper.getDate());
        directRechargePlusRequest.setAccountId(userThirdAccount.getAccountId());
        directRechargePlusRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        directRechargePlusRequest.setIdNo(userThirdAccount.getIdNo());
        directRechargePlusRequest.setName(users.getRealname());
        directRechargePlusRequest.setMobile(userThirdAccount.getMobile());
        directRechargePlusRequest.setCardNo(userThirdAccount.getCardNo());
        directRechargePlusRequest.setCurrency("156");
        directRechargePlusRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/asset/recharge/callback"));
        directRechargePlusRequest.setRetUrl(String.format("%s/%s/%s", javaDomain, "/pub/recharge/show", directRechargePlusRequest.getTxDate() + directRechargePlusRequest.getTxTime() + directRechargePlusRequest.getSeqNo()));
        directRechargePlusRequest.setLastSrvAuthCode(srvTxCode);
        directRechargePlusRequest.setSmsCode(voRechargeReq.getSmsCode());
        directRechargePlusRequest.setAcqRes(users.getId().toString());
        directRechargePlusRequest.setChannel(ChannelContant.getchannel(request));
        directRechargePlusRequest.setTxAmount(voRechargeReq.getMoney().toString());
        VoHtmlResp resp = VoBaseResp.ok("成功", VoHtmlResp.class);
        String html = jixinManager.getHtml(JixinTxCodeEnum.DIRECT_RECHARGE_PLUS, directRechargePlusRequest);


        Date now = new Date();
        // 插入充值记录
        RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
        rechargeDetailLog.setUserId(users.getId());
        rechargeDetailLog.setBankName(userThirdAccount.getBankName());
        rechargeDetailLog.setCallbackTime(null);
        rechargeDetailLog.setCreateTime(now);
        rechargeDetailLog.setUpdateTime(now);
        rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
        rechargeDetailLog.setDel(0);
        rechargeDetailLog.setIp(IpHelper.getIpAddress(request));
        rechargeDetailLog.setMobile(users.getPhone());
        rechargeDetailLog.setMoney(new Double(voRechargeReq.getMoney() * 100).longValue());
        rechargeDetailLog.setRechargeChannel(0);
        rechargeDetailLog.setSeqNo(directRechargePlusRequest.getTxDate() + directRechargePlusRequest.getTxTime() + directRechargePlusRequest.getSeqNo());

        rechargeDetailLogService.save(rechargeDetailLog);
        try {
            resp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl modifyOpenAccPwd gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }

        return ResponseEntity.ok(resp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> rechargeCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        DirectRechargePlusResponse directRechargePlusResponse = jixinManager.callback(request, new TypeToken<DirectRechargePlusResponse>() {
        });

        if (ObjectUtils.isEmpty(directRechargePlusResponse)) {
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        Long userId = Long.parseLong(directRechargePlusResponse.getAcqRes());
        if (ObjectUtils.isEmpty(userId)) {
            log.error("AssetBizImpl.rechargeCallback:  userId is null");
            return ResponseEntity.badRequest().body("success");
        }
        Users users = userService.findByIdLock(userId);
        if (ObjectUtils.isEmpty(users)) {
            log.error("AssetBizImpl.rechargeCallback:  userId is null");
            return ResponseEntity.badRequest().body("success");
        }
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            log.error("AssetBizImpl.rechargeCallback: userThirdAccount is null");
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        // 更改充值记录
        String seqNo = directRechargePlusResponse.getTxDate() + directRechargePlusResponse.getTxTime() + directRechargePlusResponse.getSeqNo();
        RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findTopBySeqNo(seqNo);
        if (ObjectUtils.isEmpty(rechargeDetailLog)) {
            log.error("AssetBizImpl.rechargeCallback: 没有该条充值记录");
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        if (JixinResultContants.SUCCESS.equals(directRechargePlusResponse.getRetCode())) {
            if (rechargeDetailLog.getState() == 1) {
                return ResponseEntity.ok("success");
            }

            if (!userId.equals(rechargeDetailLog.getUserId())) {
                log.error("AssetBizImpl.rechargeCallback: 当前充值不属于该用户");
                return ResponseEntity
                        .badRequest()
                        .body("success");
            }

            Double money = new Double(directRechargePlusResponse.getTxAmount()) * 100;
            // 验证金额
            if (rechargeDetailLog.getMoney() != money.longValue()) {
                log.error("AssetBizImpl.rechargeCallback: 充值金额不一致");
                return ResponseEntity
                        .badRequest()
                        .body("success");
            }

            Date now = new Date();
            rechargeDetailLog.setCallbackTime(now);
            rechargeDetailLog.setUpdateTime(now);
            rechargeDetailLog.setState(1);
            rechargeDetailLogService.save(rechargeDetailLog);


            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.offlineRecharge);  // 招标失败解除冻结资金
            assetChange.setUserId(userId);
            assetChange.setMoney(money.longValue());
            assetChange.setRemark(String.format("成功充值%s元", StringHelper.formatDouble(money.longValue() / 100D, true)));
            assetChange.setSourceId(rechargeDetailLog.getId());
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            assetChangeProvider.commonAssetChange(assetChange);


            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(now, 30));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
            log.info("触发短信充值");
            return ResponseEntity.ok("success");
        } else {  // 充值失败
            if (rechargeDetailLog.getState() == 2) {
                return ResponseEntity.ok("success");
            }

            Date now = new Date();
            rechargeDetailLog.setCallbackTime(now);
            rechargeDetailLog.setUpdateTime(now);
            rechargeDetailLog.setState(2); // 充值失败
            rechargeDetailLogService.save(rechargeDetailLog);
            return ResponseEntity.ok("success");
        }
    }

    @Override
    public ResponseEntity<VoAliPayRechargeInfo> alipayBankInfo(Long userId) {
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoAliPayRechargeInfo.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoAliPayRechargeInfo.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoAliPayRechargeInfo.class));
        }

        VoAliPayRechargeInfo voAliPayRechargeInfo = VoBaseResp.ok("查询成功!", VoAliPayRechargeInfo.class);
        voAliPayRechargeInfo.setBankCardNo(userThirdAccount.getAccountId());
        voAliPayRechargeInfo.setName(users.getRealname());
        voAliPayRechargeInfo.setBankName("江西银行总行营业部");
        return ResponseEntity.ok(voAliPayRechargeInfo);
    }

    @Override
    public ResponseEntity<VoRechargeEntityWrapResp> log(Long userId, int pageIndex, int pageSize) {
        pageIndex = pageIndex <= 1 ? 0 : pageIndex - 1;
        pageSize = pageSize < 0 ? 10 : pageSize;
        List<RechargeDetailLog> logs = rechargeDetailLogService.log(userId, pageIndex, pageSize);
        List<VoRechargeEntityResp> voRechargeEntityRespList = new ArrayList<>(logs.size());
        logs.stream().forEach((RechargeDetailLog value) -> {
            VoRechargeEntityResp bean = new VoRechargeEntityResp();
            bean.setRechargeMoney(StringHelper.formatDouble(value.getMoney() / 100D, true));
            String state = value.getState() == 0 ? "支付提交中" : value.getState() == 1 ? "充值成功" : "充值失败";
            String channel = value.getRechargeChannel() == 0 ? "线上网银充值" : "线下转账";
            bean.setTitle(String.format("%s-%s", channel, state));
            String cardNo = value.getCardNo().substring(value.getCardNo().length() - 4);
            bean.setBankNameAndCardNo(String.format("%s(%s)", value.getBankName(), cardNo));
            bean.setSeqNo(value.getSeqNo());
            bean.setRechargetime(DateHelper.dateToString(value.getCreateTime()));
            voRechargeEntityRespList.add(bean);
        });

        VoRechargeEntityWrapResp resp = VoBaseResp.ok("查询成功", VoRechargeEntityWrapResp.class);
        resp.getList().addAll(voRechargeEntityRespList);
        return ResponseEntity.ok(resp);
    }

    /**
     * PC:资金流水导出到excel
     *
     * @param voAssetLogReq
     * @param response
     */
    @Override
    public void pcToExcel(VoAssetLogReq voAssetLogReq, HttpServletResponse response) {

        Users user = userService.findById(voAssetLogReq.getUserId());
        voAssetLogReq.setStartTime(DateHelper.dateToString(user.getCreatedAt()));
        voAssetLogReq.setEndTime(DateHelper.dateToString(new Date()));
        List<NewAssetLog> assetLogs = assetLogService.pcToExcel(voAssetLogReq);

        List<AssetLogs> assetLogsList = new ArrayList<>(assetLogs.size());
        if (!CollectionUtils.isEmpty(assetLogs)) {
            assetLogs.stream().forEach(p -> {
                AssetLogs assetLog = new AssetLogs();
                assetLog.setOperationMoney(StringHelper.toString(p.getOpMoney() / 100D));
                assetLog.setRemark(p.getRemark());
                assetLog.setTime(DateHelper.dateToString(p.getCreateTime()));
                assetLog.setTypeName(p.getOpName());
                assetLog.setUsableMoney(StringHelper.toString(p.getUseMoney() / 100D));
                assetLogsList.add(assetLog);
            });
            LinkedHashMap<String, String> paramMaps = Maps.newLinkedHashMap();
            paramMaps.put("time", "时间");
            paramMaps.put("typeName", "交易类型");
            paramMaps.put("operationMoney", "操作金额（分）");
            paramMaps.put("usableMoney", "可用金额（分）");
            paramMaps.put("remark", "备注");
            try {
                ExcelUtil.listToExcel(assetLogsList, paramMaps, "资金流水", response);
            } catch (ExcelException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ResponseEntity<VoPreRechargeResp> preRecharge(Long userId) {
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoPreRechargeResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoPreRechargeResp.class));
        }

        if (StringUtils.isEmpty(userThirdAccount.getCardNo())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_BIND_BANK_CARD, "对不起!你的账号还没绑定银行卡呢", VoPreRechargeResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoPreRechargeResp.class));
        }

        VoPreRechargeResp voPreRechargeResp = VoBaseResp.ok("查询成功", VoPreRechargeResp.class);
        voPreRechargeResp.setBankName(userThirdAccount.getBankName());
        voPreRechargeResp.setCardNo(userThirdAccount.getCardNo().substring(userThirdAccount.getCardNo().length() - 4));
        voPreRechargeResp.setLogo(String.format("%s/%s", javaDomain, userThirdAccount.getBankLogo()));
        voPreRechargeResp.setToRechargeBankNo(String.format("江西银行电子账户(%s)", userThirdAccount.getAccountId().substring(userThirdAccount.getAccountId().length() - 5)));
        DictValue bank = null;
        try {
            bank = bankLimitCache.get(userThirdAccount.getBankName());
        } catch (Throwable e) {
            log.error("AssetBizImpl.preRecharge: bank type is exists ");
        }

        if (ObjectUtils.isEmpty(bank)) {
            voPreRechargeResp.setTimesLimit("未知");
            voPreRechargeResp.setDayLimit("未知");
            voPreRechargeResp.setMouthLimit("未知");
        }
        // 返回银行信息
        voPreRechargeResp.setTimesLimit(bank.getValue04().split(",")[0]);
        voPreRechargeResp.setDayLimit(bank.getValue05().split(",")[0]);
        voPreRechargeResp.setMouthLimit(bank.getValue06().split(",")[0]);
        return ResponseEntity.ok(voPreRechargeResp);
    }


    @Override
    public ResponseEntity<VoAssetIndexResp> asset(Long userId) {
        // 获取用户待还资金
        Asset asset = assetService.findByUserId(userId);
        if (ObjectUtils.isEmpty(asset)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求!", VoAssetIndexResp.class));
        }
        UserCache userCache = userCacheService.findById(userId);
        if (ObjectUtils.isEmpty(userCache)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求!", VoAssetIndexResp.class));
        }

        VoAssetIndexResp response = VoBaseResp.ok("查询成功", VoAssetIndexResp.class);
        response.setAccruedMoney(StringHelper.formatDouble(userCache.getIncomeTotal() / 100D, true)); //累计收益
        response.setCollectionMoney(StringHelper.formatDouble((userCache.getWaitCollectionPrincipal() + userCache.getWaitCollectionInterest()) / 100D, true)); // 待收
        response.setAccountMoney(StringHelper.formatDouble((asset.getNoUseMoney() + asset.getUseMoney()) / 100D, true));
        response.setTotalAsset(StringHelper.formatDouble((asset.getUseMoney() + asset.getNoUseMoney() + asset.getCollection() - asset.getPayment()) / 100D, true));
        Double netAmount = ((asset.getUseMoney() + userCache.getWaitCollectionPrincipal()) * 0.8D - asset.getPayment()) / 100D;
        response.setNetAmount(StringHelper.formatDouble(netAmount, true));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoAccruedMoneyResp> accruedMoney(Long userId) {
        UserCache userCache = userCacheService.findById(userId);
        if (ObjectUtils.isEmpty(userCache)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoAccruedMoneyResp.class));
        }

        Long incomeBonus = userCache.getIncomeBonus();
        Long incomeOverdue = userCache.getIncomeOverdue();
        Long incomeInterest = userCache.getIncomeInterest();
        Long incomeAward = userCache.getIncomeAward();
        Long incomeIntegralCash = userCache.getIncomeIntegralCash();
        Long incomeOther = userCache.getIncomeOther();
        Long totalIncome = incomeBonus + incomeOverdue + incomeInterest + incomeAward + incomeIntegralCash + incomeOther;
        VoAccruedMoneyResp response = VoBaseResp.ok("查询成功", VoAccruedMoneyResp.class);
        response.setIncomeBonus(StringHelper.formatMon(incomeBonus / 100D));
        response.setIncomeAward(StringHelper.formatMon(incomeAward / 100D));
        response.setIncomeInterest(StringHelper.formatMon(incomeInterest / 100D));
        response.setIncomeIntegralCash(StringHelper.formatMon(incomeIntegralCash / 100D));
        response.setIncomeOther(StringHelper.formatMon(incomeOther / 100D));
        response.setTotalIncome(StringHelper.formatMon(totalIncome / 100D));
        response.setIncomeOverdue(StringHelper.formatMon(incomeOverdue / 100D));
        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional
    public ResponseEntity<VoAvailableAssetInfoResp> accountMoney(Long userId) {
        Asset asset = assetService.findByUserIdLock(userId);
        if (ObjectUtils.isEmpty(asset)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoAvailableAssetInfoResp.class));
        }
        try {
            assetSynBiz.doAssetSyn(userId);
        } catch (Exception e) {
            log.error("用户余额同步错误", e);
        }

        VoAvailableAssetInfoResp resp = VoBaseResp.ok("查询成功", VoAvailableAssetInfoResp.class);
        Long noUserMoney = asset.getNoUseMoney();
        Long userMoney = asset.getUseMoney();
        Long total = asset.getNoUseMoney() + asset.getUseMoney();

        resp.setNoUseMoney(noUserMoney);
        resp.setViewNoUseMoney(StringHelper.formatMon(noUserMoney / 100d));
        resp.setUseMoney(userMoney);
        resp.setViewUseMoney(StringHelper.formatMon(userMoney / 100d));
        resp.setTotal(total);
        resp.setViwTotal(StringHelper.formatMon(total / 100d));
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<VoCollectionResp> collectionMoney(Long userId) {
        UserCache userCache = userCacheService.findById(userId);
        if (ObjectUtils.isEmpty(userCache)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoCollectionResp.class));
        }
        Asset asset = assetService.findByUserId(userId);
        VoCollectionResp response = VoBaseResp.ok("查询成功", VoCollectionResp.class);
        Long waitCollectionInterest = userCache.getWaitCollectionInterest();
        Long waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Long waitCollectionTotal = waitCollectionInterest + waitCollectionPrincipal;
        response.setHideInterest(waitCollectionInterest);
        response.setInterest(StringHelper.formatMon(waitCollectionInterest / 100d));

        response.setPrincipal(StringHelper.formatMon(waitCollectionPrincipal / 100d));
        response.setHidePrincipal(waitCollectionPrincipal);
        response.setWaitCollectionTotal(StringHelper.formatMon(waitCollectionTotal / 100d));
        response.setHideWaitCollectionTotal(waitCollectionTotal);
        return ResponseEntity.ok(response);
    }


    /**
     * 查询线下充值
     *
     * @param pageIndex 下标
     * @param pageSize  页面
     * @param accountId 存管账户
     * @return
     */
    private AccountDetailsQueryResponse doOffLineRecharge(int pageIndex, int pageSize, String accountId) {
        AccountDetailsQueryRequest request = new AccountDetailsQueryRequest();
        request.setAccountId(accountId);
        request.setStartDate(jixinTxDateHelper.getTxDateStr());
        request.setEndDate(jixinTxDateHelper.getTxDateStr());
        request.setChannel(ChannelContant.HTML);
        request.setType("9"); // 转入
        request.setTranType("7820"); // 线下转账的
        request.setPageSize(String.valueOf(pageSize));
        request.setPageNum(String.valueOf(pageIndex));

        AccountDetailsQueryResponse response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY, request, AccountDetailsQueryResponse.class);
        if (ObjectUtils.isEmpty(response)) {
            log.error(String.format("查询资金请求异常"));
            return null;
        }

        if (!JixinResultContants.SUCCESS.equals(response.getRetCode())) {
            log.error(String.format("资金查询失败"));
            return null;
        }

        return response;
    }

    /**
     * 账户总额统计
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<VoViewAssetStatisticWarpRes> pcAccountStatstic(Long userId) {
        try {
            VoViewAssetStatisticWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewAssetStatisticWarpRes.class);
            AssetStatistic assetStatistic = userCacheService.assetStatistic(userId);
            warpRes.setAssetStatistic(assetStatistic);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "查询异常,请稍后在试",
                            VoViewAssetStatisticWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<IncomeEarnedDetail> pcIncomeEarned(Long userId) {
        return userCacheService.incomeEarned(userId);
    }

    /**
     * 支出统计
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<ExpenditureDetail> pcExpenditureDetail(Long userId) {
        return userCacheService.expenditureDetail(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoUserAssetInfoResp> adminSynOffLineRecharge(VoSynAssetsRep voSynAssetsRep) throws Exception {
        String paramStr = voSynAssetsRep.getParamStr();
        if (!SecurityHelper.checkSign(voSynAssetsRep.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc 签名验证不通过", VoUserAssetInfoResp.class));
        }

        log.info(String.format("资金同步: %s", paramStr));
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long userId = Long.parseLong(paramMap.get("userId"));
        String date = paramMap.get("date");
        Date synDate = null;
        if (!StringUtils.isEmpty(date)) {
            synDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);
        } else {
            synDate = new Date();
        }
        assetSynBiz.doAdminSynAsset(userId, synDate);
        return userAssetInfo(userId);
    }


    @Override
    public ResponseEntity<VoUnionRechargeInfo> unionBankInfo(Long userId) {
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoUnionRechargeInfo.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoUnionRechargeInfo.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoUnionRechargeInfo.class));
        }

        VoUnionRechargeInfo voUnionRechargeInfo = VoBaseResp.ok("查询成功!", VoUnionRechargeInfo.class);
        voUnionRechargeInfo.setBankCardNo(userThirdAccount.getAccountId());
        voUnionRechargeInfo.setName(users.getRealname());
        voUnionRechargeInfo.setBankName("江西银行");
        voUnionRechargeInfo.setBranchName("江西银行总行营业部");
        return ResponseEntity.ok(voUnionRechargeInfo);
    }


    @Override
    public ResponseEntity<VoViewAssetLogWarpRes> newAssetLogResList(VoAssetLogReq voAssetLogReq) {
        VoViewAssetLogWarpRes voViewAssetLogWarpRes = VoBaseResp.ok("查询成功", VoViewAssetLogWarpRes.class);

        String startTimeStr = voAssetLogReq.getStartTime();
        if (StringUtils.isEmpty(startTimeStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询时间错误", VoViewAssetLogWarpRes.class));
        }

        String endTimeStr = voAssetLogReq.getEndTime();
        if (StringUtils.isEmpty(endTimeStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询时间错误", VoViewAssetLogWarpRes.class));
        }

        Sort sort = new Sort(
                new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = new PageRequest(voAssetLogReq.getPageIndex()
                , voAssetLogReq.getPageSize()
                , sort);
        List<String> types = Arrays.asList(AssetChangeTypeEnum.collectionAdd.getLocalType(),
                AssetChangeTypeEnum.collectionSub.getLocalType(),
                AssetChangeTypeEnum.paymentSub.getLocalType(),
                AssetChangeTypeEnum.paymentAdd.getLocalType());
        Date startTime = DateHelper.beginOfDate(DateHelper.stringToDate(voAssetLogReq.getStartTime(), DateHelper.DATE_FORMAT_YMD));
        Date endTime = DateHelper.endOfDate(DateHelper.stringToDate(voAssetLogReq.getEndTime(), DateHelper.DATE_FORMAT_YMD));
        Specification<NewAssetLog> specification = Specifications.<NewAssetLog>and()
                .between("createTime",
                        new Range<>(
                                DateHelper.beginOfDate(startTime),
                                DateHelper.endOfDate(endTime)))
                .eq("userId", voAssetLogReq.getUserId())
                .eq("del", 0)
                .notIn("localType", types.toArray())
                .build();
        Page<NewAssetLog> assetLogPage = newAssetLogService.findAll(specification, pageable);
        voViewAssetLogWarpRes.setTotalCount(assetLogPage.getTotalElements());

        List<NewAssetLog> assetLogs = assetLogPage.getContent();
        if (CollectionUtils.isEmpty(assetLogs)) {
            return ResponseEntity.ok(voViewAssetLogWarpRes);
        }

        VoViewAssetLogRes voViewAssetLogRes = null;
        for (NewAssetLog newAssetLog : assetLogs) {
            voViewAssetLogRes = new VoViewAssetLogRes();
            Long opMoney = newAssetLog.getOpMoney();
            Long userMoney = newAssetLog.getUseMoney();
            voViewAssetLogRes.setCreatedAt(DateHelper.dateToString(newAssetLog.getCreateTime()));
            if (newAssetLog.getTxFlag().equals("C")) {
                voViewAssetLogRes.setMoney("-" + new Double(opMoney / 100D).toString());
                voViewAssetLogRes.setShowMoney("-" + StringHelper.formatDouble(opMoney / 100D, true));
            } else if (newAssetLog.getTxFlag().equals("D")) {
                voViewAssetLogRes.setMoney(new Double(opMoney / 100D).toString());
                voViewAssetLogRes.setShowMoney("+" + StringHelper.formatDouble(opMoney / 100D, true));
            } else {
                voViewAssetLogRes.setMoney(new Double(opMoney / 100D).toString());
                voViewAssetLogRes.setShowMoney(StringHelper.formatDouble(opMoney / 100D, true));
            }
            voViewAssetLogRes.setUseMoney(StringHelper.formatMon(userMoney / 100D));
            voViewAssetLogRes.setHideUseMoney(userMoney / 100D);
            voViewAssetLogRes.setRemark(newAssetLog.getRemark());
            voViewAssetLogRes.setNoUseMoney(StringHelper.formatMon(newAssetLog.getNoUseMoney() / 100D));
            voViewAssetLogRes.setTypeName(newAssetLog.getOpName());
            voViewAssetLogWarpRes.getResList().add(voViewAssetLogRes);
        }
        return ResponseEntity.ok(voViewAssetLogWarpRes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoAssetIndexResp> synHome(Long userId) throws Exception {
        try {
            assetSynBiz.doAssetSyn(userId);
        } catch (Exception e) {
            log.error("资金同步异常", e);
        }
        return asset(userId);
    }

    @Override
    public ResponseEntity<VoQueryInfoResp> queryUserMoneyForJixin(VoDoAgainVerifyReq voDoAgainVerifyReq) {
        String paramStr = voDoAgainVerifyReq.getParamStr();
        if (!SecurityHelper.checkSign(voDoAgainVerifyReq.getSign(), paramStr)) {
            log.error("BorrowBizImpl doAgainVerify error：签名校验不通过");
        }

        Map<String, String> paramMap = GSON.fromJson(paramStr, new com.google.gson.reflect.TypeToken<Map<String, String>>() {
        }.getType());
        String userId = paramMap.get("userId");
        if (StringUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "没有当前用户", VoQueryInfoResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(userId));
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户", VoQueryInfoResp.class));
        }

        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            String msg = ObjectUtils.isEmpty(balanceQueryResponse) ? "当前网络异常, 请稍后尝试!" : balanceQueryResponse.getRetMsg();
            log.error(String.format("资金同步: %s", msg));
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg, VoQueryInfoResp.class));
        }

        VoQueryInfoResp voQueryInfoResp = VoBaseResp.ok("查询成功", VoQueryInfoResp.class);
        voQueryInfoResp.setTotalMoey(balanceQueryResponse.getCurrBal());
        voQueryInfoResp.setValidateMoney(balanceQueryResponse.getAvailBal());
        return ResponseEntity.ok(voQueryInfoResp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> cancelRedPacket(VoUnsendRedPacket voUnsendRedPacket) throws Exception {
        String paramStr = voUnsendRedPacket.getParamStr();/* pc请求提前结清参数 */
        if (!SecurityHelper.checkSign(voUnsendRedPacket.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }


        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String newAssetLogId = paramMap.get("id");
        NewAssetLog newAssetLog = newAssetLogService.findById(Long.parseLong(newAssetLogId));
        if (!newAssetLog.getLocalType().equals("receiveRedpack")) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户资金记录不属于红包派发记录"));
        }

        if (ObjectUtils.isEmpty(newAssetLog)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "没有资金变动用户记录!"));
        }

        Long userId = newAssetLog.getUserId();
        Asset asset = assetService.findByUserIdLock(userId);
        if (ObjectUtils.isEmpty(asset)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "该用户资金记录!"));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户开户信息为空!"));
        }

        String localSeqNo = newAssetLog.getLocalSeqNo();
        long redId = 0;
        try {
            redId = assetChangeProvider.getRedpackAccountId();
        } catch (ExecutionException e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询红包账户为空!"));
        }

        UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redId);
        if (ObjectUtils.isEmpty(redpackAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询红包账户为空!"));
        }

        if (asset.getUseMoney() - newAssetLog.getOpMoney() < 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户资金额小于撤销金额!"));
        }

        // 20170909101039117204
        String txDate = localSeqNo.substring(0, 8);
        String txTime = localSeqNo.substring(8, 14);
        String seqNo = localSeqNo.substring(14);
        VoucherPayCancelRequest voucherPayCancelRequest = new VoucherPayCancelRequest();
        voucherPayCancelRequest.setAccountId(redpackAccount.getAccountId());
        voucherPayCancelRequest.setTxAmount(StringHelper.formatDouble(newAssetLog.getOpMoney() / 100D, false));
        voucherPayCancelRequest.setOrgTxDate(txDate);
        voucherPayCancelRequest.setOrgTxTime(txTime);
        voucherPayCancelRequest.setForAccountId(userThirdAccount.getAccountId());
        voucherPayCancelRequest.setOrgSeqNo(seqNo);
        voucherPayCancelRequest.setAcqRes(String.valueOf(userId));
        voucherPayCancelRequest.setChannel(ChannelContant.HTML);
        VoucherPayCancelResponse response = jixinManager.send(JixinTxCodeEnum.UNSEND_RED_PACKET, voucherPayCancelRequest, VoucherPayCancelResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络出现异常, 请稍后尝试！" : response.getRetMsg();
            log.error(msg);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, String.format("撤销红包失败：%s", response.getRetMsg())));
        }

        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        // 红包取消派发
        AssetChange redpackPublish = new AssetChange();
        redpackPublish.setMoney(newAssetLog.getOpMoney());
        redpackPublish.setType(AssetChangeTypeEnum.cancelPaltFormRedpack);  //  取消发放红包
        redpackPublish.setUserId(redId);
        redpackPublish.setForUserId(userId);
        redpackPublish.setRemark(String.format("取消派发奖励红包 %s元", StringHelper.formatDouble(newAssetLog.getOpMoney() / 100D, true)));
        redpackPublish.setGroupSeqNo(groupSeqNo);
        redpackPublish.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
        redpackPublish.setSourceId(newAssetLog.getId());
        assetChangeProvider.commonAssetChange(redpackPublish);

        // 红包取消领取
        AssetChange redpackR = new AssetChange();
        redpackR.setMoney(newAssetLog.getOpMoney());
        redpackR.setType(AssetChangeTypeEnum.revokedRedpack);
        redpackR.setUserId(userId);
        redpackR.setForUserId(redId);
        redpackR.setRemark(String.format("取消奖励红包 %s元", StringHelper.formatDouble(newAssetLog.getOpMoney() / 100D, true)));
        redpackR.setGroupSeqNo(groupSeqNo);
        redpackR.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
        redpackR.setSourceId(newAssetLog.getId());
        assetChangeProvider.commonAssetChange(redpackR);

        return ResponseEntity.ok(VoBaseResp.ok(String.format("撤销红包成功：msg->%s", response.getRetMsg())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> adminRechargeForm(VoAdminRechargeReq voAdminRechargeReq) throws Exception {
        String paramStr = voAdminRechargeReq.getParamStr();
        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String id = paramMap.get("id");
        long rechargeId = Long.parseLong(id);
        RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findById(rechargeId);
        if (ObjectUtils.isEmpty(rechargeDetailLog)) {
            log.error("AssetBizImpl.rechargeCallback: 没有该条充值记录");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "没有该条充值记录"));
        }

        if (rechargeDetailLog.getState().intValue() == 1) {
            log.error("AssetBizImpl.rechargeCallback: 当前标的已经充值成功");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前标的已经充值成功"));
        }

        Date now = new Date();
        rechargeDetailLog.setUpdateTime(now);
        rechargeDetailLog.setState(1);
        rechargeDetailLog.setRemark("平台审核通过");
        rechargeDetailLogService.save(rechargeDetailLog);

        AssetChange assetChange = new AssetChange();
        assetChange.setType(AssetChangeTypeEnum.offlineRecharge);  // 招标失败解除冻结资金
        assetChange.setUserId(rechargeDetailLog.getUserId());
        assetChange.setMoney(rechargeDetailLog.getMoney());
        assetChange.setRemark(String.format("成功充值%s元", StringHelper.formatDouble(rechargeDetailLog.getMoney() / 100D, true)));
        assetChange.setSourceId(rechargeDetailLog.getId());
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChangeProvider.commonAssetChange(assetChange);

        // 触发用户充值
        MqConfig mqConfig = new MqConfig();
        mqConfig.setTag(MqTagEnum.RECHARGE);
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
        mqConfig.setSendTime(DateHelper.addSeconds(now, 30));
        ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
        mqConfig.setMsg(body);
        mqHelper.convertAndSend(mqConfig);
        return ResponseEntity.ok(VoBaseResp.ok("审核成功"));
    }

    @Override
    public ResponseEntity<VoBaseResp> cancelRedPacketNoChangeLog(VoUnsendRedPacket voUnsendRedPacket) {
        String paramStr = voUnsendRedPacket.getParamStr();/* pc请求提前结清参数 */
        if (!SecurityHelper.checkSign(voUnsendRedPacket.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }

        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String localSeqNo = paramMap.get("seqNo");
        String money = paramMap.get("money");
        String userId = paramMap.get("userId");

        UserThirdAccount userThird = userThirdAccountService.findByUserId(Long.parseLong(userId));
        if (ObjectUtils.isEmpty(userThird)) {
            log.error("当前用户没有开户");
        }
        long redId = 0;
        try {
            redId = assetChangeProvider.getRedpackAccountId();
        } catch (ExecutionException e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询红包账户为空!"));
        }

        UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redId);
        if (ObjectUtils.isEmpty(redpackAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询红包账户为空!"));
        }

        String txDate = localSeqNo.substring(0, 8);
        String txTime = localSeqNo.substring(8, 14);
        String seqNo = localSeqNo.substring(14);
        VoucherPayCancelRequest voucherPayCancelRequest = new VoucherPayCancelRequest();
        voucherPayCancelRequest.setAccountId(redpackAccount.getAccountId());
        voucherPayCancelRequest.setTxAmount(money);
        voucherPayCancelRequest.setOrgTxDate(txDate);
        voucherPayCancelRequest.setOrgTxTime(txTime);
        voucherPayCancelRequest.setForAccountId(userThird.getAccountId());
        voucherPayCancelRequest.setOrgSeqNo(seqNo);
        voucherPayCancelRequest.setAcqRes(userThird.getAccountId());
        voucherPayCancelRequest.setChannel(ChannelContant.HTML);

        VoucherPayCancelResponse response = jixinManager.send(JixinTxCodeEnum.UNSEND_RED_PACKET, voucherPayCancelRequest, VoucherPayCancelResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络出现异常, 请稍后尝试！" : response.getRetMsg();
            log.error(msg);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, String.format("撤销红包失败：%s", response.getRetMsg())));
        }
        log.error("请求撤销用户红包" + new Gson().toJson(voucherPayCancelRequest));
        return ResponseEntity.ok(VoBaseResp.ok(String.format("撤销红包成功：msg->%s", response.getRetMsg())));
    }

    @Override
    @Transactional(rollbackFor = ExcelException.class)
    public ResponseEntity<String> offlineRechargeCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String bgData = request.getParameter("bgData");
        log.info("==================================");
        log.info(String.format("即信线下充值回调: 数据[%s]", bgData));
        log.info("==================================");
        try {
            OfflineRechargeCallbackResponse offlineRechargeCallbackResponse = jixinManager.specialCallback(request,
                    new TypeToken<OfflineRechargeCallbackResponse>() {
                    });

            if (ObjectUtils.isEmpty(offlineRechargeCallbackResponse)) {
                exceptionEmailHelper.sendErrorMessage("线下充值回调通知异常", bgData);
                return ResponseEntity.ok("success");
            }

            String accountId = offlineRechargeCallbackResponse.getAccountId(); // 当前账户类型
            String orgSeqNo = offlineRechargeCallbackResponse.getOrgSeqNo();  // 原始流水号
            String orgTxDate = offlineRechargeCallbackResponse.getOrgTxDate(); // 原始日期
            String orgTxTime = offlineRechargeCallbackResponse.getOrgTxTime(); // 原始时间
            String txAmount = offlineRechargeCallbackResponse.getTxAmount(); // 交易金额
            String seqNo = String.format("%s%s%s", orgTxDate, orgTxTime, orgSeqNo);
            RechargeDetailLog existsRechargeDatailLog = rechargeDetailLogService.findTopBySeqNo(seqNo);
            if (!ObjectUtils.isEmpty(existsRechargeDatailLog)) {  // 重复调用
                log.error(String.format("线下充值回调接口, 重复调用充值接口: 数据[%s]", bgData));
                return ResponseEntity.ok("success");
            }

            UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(accountId);
            Preconditions.checkNotNull(userThirdAccount, "线下充值, 当前开户信息为空");
            Long userId = userThirdAccount.getUserId();
            Date nowDate = new Date();
            Date synDate = DateHelper.stringToDate(orgTxDate, DateHelper.DATE_FORMAT_YMD_NUM);
            // 写入线下充值日志
            Double recordRecharge = new Double(MoneyHelper.multiply(txAmount, "100", 0));
            Long money = recordRecharge.longValue();
            RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
            rechargeDetailLog.setUserId(userId);
            rechargeDetailLog.setBankName(userThirdAccount.getBankName());
            rechargeDetailLog.setCallbackTime(nowDate);
            rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
            rechargeDetailLog.setDel(0);
            rechargeDetailLog.setState(1); // 充值成功
            rechargeDetailLog.setMoney(money.longValue());
            rechargeDetailLog.setRechargeChannel(1);  // 其他渠道
            rechargeDetailLog.setRechargeType(1); // 线下充值
            rechargeDetailLog.setSeqNo(seqNo);
            rechargeDetailLog.setCreateTime(synDate);
            rechargeDetailLog.setUpdateTime(nowDate);
            rechargeDetailLogService.save(rechargeDetailLog);

            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.offlineRecharge);
            assetChange.setUserId(userId);
            assetChange.setMoney(money.longValue());
            assetChange.setRemark(String.format("成功线下充值%s元", StringHelper.formatDouble(money.longValue() / 100D, true)));
            assetChange.setSourceId(rechargeDetailLog.getId());
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            assetChangeProvider.commonAssetChange(assetChange);

            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 30));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
        } catch (Exception e) {
            log.error(String.format("线下充值回调接口, 发生异常: 数据[%s]", bgData), e);
            exceptionEmailHelper.sendException(String.format("线下充值回调接口, 发生异常: 数据[%s]", bgData), e);
            throw new Exception(e);
        }


        return ResponseEntity.ok("success");
    }


    /**
     * 安全查询线下充值记录
     *
     * @param offlineRechargeCallRequest
     * @param retryNum
     * @return
     */
    private OfflineRechargeCallResponse safeOfflineRechargeCallBack(OfflineRechargeCallRequest offlineRechargeCallRequest, int retryNum) {
        if (retryNum <= 0) {
            log.error("安全查询线下充值记录严重BUG");
            return null;
        }

        OfflineRechargeCallResponse offlineRechargeCallResponse = jixinManager.send(JixinTxCodeEnum.OFFLINE_RECHARGE_CALL,
                offlineRechargeCallRequest,
                OfflineRechargeCallResponse.class);
        if (ObjectUtils.isEmpty(offlineRechargeCallResponse)  // 意外情况
                || (JixinResultContants.ERROR_502.equalsIgnoreCase(offlineRechargeCallResponse.getRetCode())) // 502
                || (JixinResultContants.ERROR_504.equalsIgnoreCase(offlineRechargeCallResponse.getRetCode())) // 504
                || (JixinResultContants.ERROR_JX900032.equalsIgnoreCase(offlineRechargeCallResponse.getRetCode()))) { // 频率超限

            try {
                Thread.sleep(2 * 1000);
            } catch (Exception e) {
            }

            return safeOfflineRechargeCallBack(offlineRechargeCallRequest, retryNum - 1);
        }

        return offlineRechargeCallResponse;
    }

}
