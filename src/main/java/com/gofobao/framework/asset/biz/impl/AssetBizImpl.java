package com.gofobao.framework.asset.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.contants.SrvTxCodeContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.direct_recharge_online.DirectRechargeOnlineRequest;
import com.gofobao.framework.api.model.direct_recharge_online.DirectRechargeOnlineResponse;
import com.gofobao.framework.api.model.direct_recharge_plus.DirectRechargePlusRequest;
import com.gofobao.framework.api.model.direct_recharge_plus.DirectRechargePlusResponse;
import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.request.VoSynAssetsRep;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.asset.vo.response.pc.AssetLogs;
import com.gofobao.framework.asset.vo.response.pc.VoViewAssetLogsWarpRes;
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
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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
import java.util.concurrent.TimeUnit;

import static com.gofobao.framework.helper.project.UserHelper.getAssetTypeStr;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
@Slf4j
public class AssetBizImpl implements AssetBiz {

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    AssetService assetService;

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
    MqHelper mqHelper;

    @Autowired
    DictValueService dictValueServcie;

    @Autowired
    DictItemService dictItemService;

    @Autowired
    BankAccountBizImpl bankAccountBiz;

    @Autowired
    AssetChangeProvider assetChangeProvider;


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
        Long waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Long payment = asset.getPayment();
        long netWorthQuota = new Double((useMoney + waitCollectionPrincipal) * 0.8 - payment).longValue();//计算净值额度

        VoUserAssetInfoResp voUserAssetInfoResp = VoBaseResp.ok("成功", VoUserAssetInfoResp.class);
        voUserAssetInfoResp.setHideUserMoney(StringHelper.formatDouble(useMoney / 100D,true));
        voUserAssetInfoResp.setHideNoUseMoney(StringHelper.formatDouble(asset.getNoUseMoney() / 100D, true));
        voUserAssetInfoResp.setHidePayment(StringHelper.formatDouble(payment / 100D, true));
        voUserAssetInfoResp.setHideCollection(StringHelper.formatDouble(asset.getCollection() / 100D, true));
        voUserAssetInfoResp.setHideVirtualMoney(StringHelper.formatDouble(asset.getVirtualMoney() / 100D, true));
        voUserAssetInfoResp.setHideNetWorthQuota(StringHelper.formatDouble(netWorthQuota / 100D, true));
        voUserAssetInfoResp.setUseMoney(useMoney);
        voUserAssetInfoResp.setNoUseMoney( asset.getNoUseMoney());
        voUserAssetInfoResp.setPayment( asset.getPayment()) ;
        voUserAssetInfoResp.setCollection(asset.getCollection());
        voUserAssetInfoResp.setVirtualMoney( asset.getVirtualMoney() );
        voUserAssetInfoResp.setNetWorthQuota( netWorthQuota );
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
        if(StringUtils.isEmpty(userThirdAccount.getCardNo())){
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
       /* // 充值额度
        double[] rechargeCredit = bankAccountBiz.getRechargeCredit(voRechargeReq.getUserId());
        // 判断单笔额度
        double oneTimes = rechargeCredit[0];
        if (voRechargeReq.getMoney() > oneTimes) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.appendByTail("%s每笔最大充值额度为%s元",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(oneTimes, true))));
        }

        // 判断当天额度
        double dayTimes = rechargeCredit[1];
        if ((dayTimes <= 0) || (dayTimes - voRechargeReq.getMoney() < 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.appendByTail("今天你在%s的剩余充值额度%s",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(dayTimes < 0 ? 0 : dayTimes, true))));
        }

        // 判断每月额度
        double mouthTimes = rechargeCredit[2];
        if ((mouthTimes <= 0) || (mouthTimes - voRechargeReq.getMoney() < 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.appendByTail("当月你在%s的剩余充值额度%s元",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(mouthTimes < 0 ? 0 : mouthTimes, true))));
        }*/

        DirectRechargeOnlineRequest directRechargeOnlineRequest = new DirectRechargeOnlineRequest();
        directRechargeOnlineRequest.setSeqNo(RandomHelper.generateNumberCode(6));
        directRechargeOnlineRequest.setTxTime(DateHelper.getTime());
        directRechargeOnlineRequest.setTxDate(DateHelper.getDate());
        directRechargeOnlineRequest.setAccountId(userThirdAccount.getAccountId());
        directRechargeOnlineRequest.setIdType(IdTypeContant.ID_CARD);
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
        if (!directRechargeOnlineResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            log.error(String.format("请求即信联机充值异常: %s", gson.toJson(directRechargeOnlineResponse)));
            state = 2;
            msg = directRechargeOnlineResponse.getRetMsg();
        } else {
            log.info(String.format("充值成功: %s", gson.toJson(directRechargeOnlineResponse)));
            state = 1;
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
        rechargeDetailLog.setMoney(new Double(voRechargeReq.getMoney() * 100).longValue());
        rechargeDetailLog.setRechargeChannel(0);
        rechargeDetailLog.setState(state); // 充值成功
        rechargeDetailLog.setSeqNo(directRechargeOnlineRequest.getTxDate() + directRechargeOnlineRequest.getTxTime() + directRechargeOnlineRequest.getSeqNo());
        rechargeDetailLog.setResponseMessage(gson.toJson(directRechargeOnlineResponse));  // 响应吗
        rechargeDetailLogService.save(rechargeDetailLog);

        if (state == 1) {
            // 触发用户充值
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
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("充值失败！%s", msg)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoUserAssetInfoResp> synOffLineRecharge(Long userId) throws Exception {
        Users users = userService.findByIdLock(userId);
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户已被系统锁定, 如有疑问请联系客服!", VoUserAssetInfoResp.class));
        }

        // 判断开户状态
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoUserAssetInfoResp.class));
        }

        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoUserAssetInfoResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoUserAssetInfoResp.class));
        }

        if (userThirdAccount.getAutoTransferState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动债权转让协议！", VoUserAssetInfoResp.class));
        }


        if (userThirdAccount.getAutoTenderState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动投标协议！", VoUserAssetInfoResp.class));
        }


        // 查询当天充值记录
        int pageSize = 20, pageIndex = 1, realSize = 0;
        String accountId = userThirdAccount.getAccountId();  // 存管账户ID
        Date nowDate = new Date();
        do {
            AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
            accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
            accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
            accountDetailsQueryRequest.setStartDate(jixinTxDateHelper.getTxDateStr()); // 查询当天数据
            accountDetailsQueryRequest.setEndDate(jixinTxDateHelper.getTxDateStr());
            accountDetailsQueryRequest.setType("9");
            accountDetailsQueryRequest.setTranType("7820"); //  线下转账
            accountDetailsQueryRequest.setAccountId(accountId);

            AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                    accountDetailsQueryRequest,
                    AccountDetailsQueryResponse.class);

            if ((ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg();
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, msg, VoUserAssetInfoResp.class));
            }

            String subPacks = accountDetailsQueryResponse.getSubPacks();
            if (StringUtils.isEmpty(subPacks)) {
                break;
            }

            Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
            }.getType()));
            List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList());
            realSize = accountDetailsQueryItems.size();

            String seqNo;
            for (AccountDetailsQueryItem accountDetailsQueryItem : accountDetailsQueryItems) {   // 同步系统充值
                seqNo = String.format("%s%s%s", accountDetailsQueryItem.getInpDate(), accountDetailsQueryItem.getInpTime(), accountDetailsQueryItem.getTraceNo());
                RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findTopBySeqNo(seqNo);
                if (!ObjectUtils.isEmpty(rechargeDetailLog)) {
                    continue;
                }
                Double money = new Double(accountDetailsQueryItem.getTxAmount()) * 100;

                // 根据对手
                // 插入该条记录
                rechargeDetailLog = new RechargeDetailLog();
                rechargeDetailLog.setUserId(users.getId());
                rechargeDetailLog.setBankName(userThirdAccount.getBankName());
                rechargeDetailLog.setCallbackTime(nowDate);
                rechargeDetailLog.setCreateTime(nowDate);
                rechargeDetailLog.setUpdateTime(nowDate);
                rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
                rechargeDetailLog.setDel(0);
                rechargeDetailLog.setState(1); // 充值成功
                rechargeDetailLog.setMoney(money.longValue());
                rechargeDetailLog.setRechargeChannel(1);  // 其他渠道
                rechargeDetailLog.setRechargeType(1); // 线下充值
                rechargeDetailLog.setSeqNo(seqNo);
                rechargeDetailLogService.save(rechargeDetailLog);

                AssetChange assetChange = new AssetChange();
                assetChange.setType(AssetChangeTypeEnum.offlineRecharge);  // 招标失败解除冻结资金
                assetChange.setUserId(userId);
                assetChange.setMoney(money.longValue());
                assetChange.setRemark(String.format("成功充值%s元", StringHelper.formatDouble(money.longValue() / 100D, true)));
                assetChange.setSourceId(rechargeDetailLog.getId());
                assetChange.setSeqNo(assetChangeProvider.getSeqNo());
                assetChange.setGroupSeqNo(assetChangeProvider.getSeqNo());
                assetChangeProvider.commonAssetChange(assetChange);

                // 触发用户充值
                MqConfig mqConfig = new MqConfig();
                mqConfig.setTag(MqTagEnum.RECHARGE);
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
                mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 30));
                ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
                mqConfig.setMsg(body);
                mqHelper.convertAndSend(mqConfig);
            }
        } while (realSize == pageSize);
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
        directRechargePlusRequest.setIdType(IdTypeContant.ID_CARD);
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
            assetChange.setGroupSeqNo(assetChangeProvider.getSeqNo());
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
        List<AssetLog> assetLogs = assetLogService.pcToExcel(voAssetLogReq);

        List<AssetLogs> assetLogsList = new ArrayList<>(assetLogs.size());
        if (!CollectionUtils.isEmpty(assetLogs)) {
            assetLogs.stream().forEach(p -> {
                AssetLogs assetLog = new AssetLogs();
                assetLog.setOperationMoney(StringHelper.formatMon(p.getMoney() / 100D));
                assetLog.setRemark(p.getRemark());
                assetLog.setTime(DateHelper.dateToString(p.getCreatedAt()));
                assetLog.setTypeName(getAssetTypeStr(p.getType()));
                assetLog.setUsableMoney(StringHelper.formatMon(p.getUseMoney() / 100D));
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

        if(StringUtils.isEmpty(userThirdAccount.getCardNo())){
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
        response.setTotalAsset(StringHelper.formatDouble((asset.getUseMoney() + asset.getNoUseMoney() + asset.getCollection()) / 100D, true));
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
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (!ObjectUtils.isEmpty(userThirdAccount)) {
            // 查询用户资金
            BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
            balanceQueryRequest.setChannel(ChannelContant.HTML);
            balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
            BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
            if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
                String msg = ObjectUtils.isEmpty(balanceQueryResponse) ? "当前网络异常, 请稍后尝试!" : balanceQueryResponse.getRetMsg();
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, msg, VoAvailableAssetInfoResp.class));
            }

            double availBal = MathHelper.myRound(NumberHelper.toDouble(balanceQueryResponse.getAvailBal()) * 100.0,2);// 可用余额  账面余额-可用余额=冻结金额
            double currBal = MathHelper.myRound(NumberHelper.toDouble(balanceQueryResponse.getCurrBal()) * 100.0,2);// 账面余额  账面余额-可用余额=冻结金额


            // 查询用户操作记录
            int pageSize = 20, pageIndex = 1, realSize = 0;
            String accountId = userThirdAccount.getAccountId();  // 存管账户ID
            do {
                AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
                accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
                accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
                accountDetailsQueryRequest.setStartDate(jixinTxDateHelper.getTxDateStr()); // 查询当天数据
                accountDetailsQueryRequest.setEndDate(jixinTxDateHelper.getTxDateStr());
                accountDetailsQueryRequest.setType("0");
                accountDetailsQueryRequest.setAccountId(accountId);

                AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                        accountDetailsQueryRequest,
                        AccountDetailsQueryResponse.class);

                if ((ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
                    String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg();
                    return ResponseEntity
                            .badRequest()
                            .body(VoBaseResp.error(VoBaseResp.ERROR, msg, VoAvailableAssetInfoResp.class));
                }

                String subPacks = accountDetailsQueryResponse.getSubPacks();
                if (StringUtils.isEmpty(subPacks)) {
                    break;
                }

                Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
                }.getType()));
                List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList());
                realSize = accountDetailsQueryItems.size();
                pageIndex++;
            } while (realSize == pageSize);
        }
        VoAvailableAssetInfoResp resp = VoBaseResp.ok("查询成功", VoAvailableAssetInfoResp.class);
        Long noUserMoney = asset.getNoUseMoney();
        Long userMoney = asset.getUseMoney();
        Long total = (asset.getNoUseMoney() + asset.getUseMoney());

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

        VoCollectionResp response = VoBaseResp.ok("查询成功", VoCollectionResp.class);
        Long waitCollectionInterest = userCache.getWaitCollectionInterest();
        Long waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Long waitCollectionTotal = userCache.getWaitCollectionPrincipal() + userCache.getWaitCollectionInterest();
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
        return synOffLineRecharge(userId);
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
        Date startTime = DateHelper.beginOfDate(DateHelper.stringToDate(voAssetLogReq.getStartTime(), DateHelper.DATE_FORMAT_YMD));
        Date endTime = DateHelper.endOfDate(DateHelper.stringToDate(voAssetLogReq.getEndTime(), DateHelper.DATE_FORMAT_YMD));
        Specification<NewAssetLog> specification = Specifications.<NewAssetLog>and()
                .between("createTime",
                        new Range<>(
                                DateHelper.beginOfDate(startTime),
                                DateHelper.endOfDate(endTime)))
                .eq("userId", voAssetLogReq.getUserId())
                .build();
        Page<NewAssetLog> assetLogPage = newAssetLogService.findAll(specification, pageable);
        voViewAssetLogWarpRes.setTotalCount(assetLogPage.getTotalElements());

        List<NewAssetLog> assetLogs = assetLogPage.getContent();
        if (CollectionUtils.isEmpty(assetLogs)) {
            return ResponseEntity.ok(voViewAssetLogWarpRes) ;
        }

        VoViewAssetLogRes voViewAssetLogRes = null ;
        for(NewAssetLog newAssetLog : assetLogs){
            voViewAssetLogRes = new VoViewAssetLogRes() ;
            Long opMoney=newAssetLog.getOpMoney();
            Long userMoney=newAssetLog.getUseMoney();
            voViewAssetLogRes.setCreatedAt(DateHelper.dateToString(newAssetLog.getCreateTime()));
            if(newAssetLog.getTxFlag().equals("C")){
                voViewAssetLogRes.setMoney("-" + new Double(opMoney / 100D).toString());
                voViewAssetLogRes.setShowMoney("-" + StringHelper.formatDouble(opMoney / 100D, true));
            }else if(newAssetLog.getTxFlag().equals("D")){
                voViewAssetLogRes.setMoney(new Double(opMoney/ 100D).toString());
                voViewAssetLogRes.setShowMoney( "+" + StringHelper.formatDouble(opMoney / 100D, true));
            }else{
                voViewAssetLogRes.setMoney(new Double(opMoney / 100D).toString());
                voViewAssetLogRes.setShowMoney(StringHelper.formatDouble(opMoney/ 100D, true));
            }
            voViewAssetLogRes.setUseMoney(StringHelper.formatMon(userMoney/100D));
            voViewAssetLogRes.setHideUseMoney(userMoney/100D);
            voViewAssetLogRes.setRemark(newAssetLog.getRemark());
            voViewAssetLogRes.setTypeName(newAssetLog.getOpName());
            voViewAssetLogWarpRes.getResList().add(voViewAssetLogRes) ;
        }
        return ResponseEntity.ok(voViewAssetLogWarpRes);
    }


}
