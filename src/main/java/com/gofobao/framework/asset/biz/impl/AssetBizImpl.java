package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.contants.SrvTxCodeContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.direct_recharge_online.DirectRechargeOnlineRequest;
import com.gofobao.framework.api.model.direct_recharge_online.DirectRechargeOnlineResponse;
import com.gofobao.framework.api.model.direct_recharge_plus.DirectRechargePlusRequest;
import com.gofobao.framework.api.model.direct_recharge_plus.DirectRechargePlusResponse;
import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.asset.vo.response.pc.AssetLogs;
import com.gofobao.framework.asset.vo.response.pc.VoViewAssetLogsWarpRes;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.IpHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.tender.vo.response.VoViewAutoTenderList;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
@Slf4j
public class AssetBizImpl implements AssetBiz {

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
    CapitalChangeHelper capitalChangeHelper;

    @Autowired
    MqHelper mqHelper;

    @Autowired
    DictValueService dictValueServcie;

    @Autowired
    DictItemServcie dictItemServcie;

    @Autowired
    BankAccountBizImpl bankAccountBiz;


    LoadingCache<String, DictValue> bankLimitCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemServcie.findTopByAliasCodeAndDel("PLATFORM_BANK", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    return dictValueServcie.findTopByItemIdAndValue02(dictItem.getId(), bankName);
                }
            });

    private final Gson GSON = new Gson();

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

        Integer useMoney = asset.getUseMoney();
        Integer waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Integer payment = asset.getPayment();
        int netWorthQuota = new Double((useMoney + waitCollectionPrincipal) * 0.8 - payment).intValue();//计算净值额度

        VoUserAssetInfoResp voUserAssetInfoResp = VoBaseResp.ok("成功", VoUserAssetInfoResp.class);
        voUserAssetInfoResp.setUseMoney(useMoney);
        voUserAssetInfoResp.setNoUseMoney(asset.getNoUseMoney());
        voUserAssetInfoResp.setPayment(payment);
        voUserAssetInfoResp.setCollection(asset.getCollection());
        voUserAssetInfoResp.setVirtualMoney(asset.getVirtualMoney());
        voUserAssetInfoResp.setNetWorthQuota(netWorthQuota);
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
     * @param voAssetLogReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewAssetLogsWarpRes> pcAssetLogs(VoAssetLogReq voAssetLogReq) {
        try {
            VoViewAssetLogsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewAssetLogsWarpRes.class);
            List<AssetLogs> resList = assetLogService.pcAssetLogs(voAssetLogReq);
            warpRes.setTotalCount(0);
            if(!CollectionUtils.isEmpty(resList)){
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
    public ResponseEntity<VoBaseResp> rechargeOnline(HttpServletRequest request, VoRechargeReq voRechargeReq) throws Exception {
        Users users = userService.findById(voRechargeReq.getUserId());
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！"));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voRechargeReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！"));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！"));
        }


        userThirdAccount.setMobile("13008875126");
        String smsSeq = null;
        try {

            smsSeq = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.DIRECT_RECHARGE_ONLINE, userThirdAccount.getMobile()), null);
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.DIRECT_RECHARGE_ONLINE, userThirdAccount.getMobile()));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl rechargeOnline get redis exception ", e);
        }

        if (StringUtils.isEmpty(smsSeq)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取"));
        }

        // 提现额度判断
        // 判断提现额度剩余
        double[] rechargeCredit = bankAccountBiz.getCashCredit(voRechargeReq.getUserId());
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

        // 判断每月额度
        double mouthTimes = rechargeCredit[2];
        if ((mouthTimes <= 0) || (mouthTimes - voRechargeReq.getMoney() < 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("当月你在%s的剩余充值额度%s元",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(mouthTimes < 0 ? 0 : mouthTimes, true))));
        }

        DirectRechargeOnlineRequest directRechargeOnlineRequest = new DirectRechargeOnlineRequest();
        directRechargeOnlineRequest.setSeqNo(RandomHelper.generateNumberCode(6));
        directRechargeOnlineRequest.setTxTime(DateHelper.getTime());
        directRechargeOnlineRequest.setTxDate(DateHelper.getDate());
        directRechargeOnlineRequest.setAccountId(userThirdAccount.getAccountId());
        directRechargeOnlineRequest.setIdType(IdTypeContant.ID_CARD);
        directRechargeOnlineRequest.setIdNo(userThirdAccount.getIdNo());
        directRechargeOnlineRequest.setName(users.getRealname());
        directRechargeOnlineRequest.setMobile(userThirdAccount.getMobile());
        directRechargeOnlineRequest.setCardNo(userThirdAccount.getCardNo());
        directRechargeOnlineRequest.setCurrency("156");
        directRechargeOnlineRequest.setSmsSeq(smsSeq);
        directRechargeOnlineRequest.setSmsCode(voRechargeReq.getSmsCode());
        directRechargeOnlineRequest.setAcqRes(users.getId().toString());
        directRechargeOnlineRequest.setChannel(ChannelContant.getchannel(request));
        directRechargeOnlineRequest.setTxAmount(voRechargeReq.getMoney().toString());
        VoHtmlResp resp = VoBaseResp.ok("成功", VoHtmlResp.class);
        DirectRechargeOnlineResponse directRechargeOnlineResponse = jixinManager.send(JixinTxCodeEnum.DIRECT_RECHARGE_ONLINE, directRechargeOnlineRequest, DirectRechargeOnlineResponse.class);
        if (ObjectUtils.isEmpty(directRechargeOnlineResponse)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不稳定, 请稍后重试!"));
        }
        Gson gson = new Gson();
        Integer state = 0;
        if (!directRechargeOnlineResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            log.error(String.format("请求即信联机充值异常: %s", gson.toJson(directRechargeOnlineResponse)));
            state = 2;
        } else {
            state = 1;
            CapitalChangeEntity capitalChangeEntity = new CapitalChangeEntity();
            capitalChangeEntity.setToUserId(userThirdAccount.getUserId());
            capitalChangeEntity.setUserId(userThirdAccount.getUserId());
            capitalChangeEntity.setMoney(new Double(voRechargeReq.getMoney() * 100).intValue());
            capitalChangeEntity.setRemark("充值成功");
            capitalChangeEntity.setType(CapitalChangeEnum.Recharge);
            capitalChangeHelper.capitalChange(capitalChangeEntity);
        }

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
        rechargeDetailLog.setState(state); // 充值成功
        rechargeDetailLog.setSeqNo(directRechargeOnlineRequest.getTxDate() + directRechargeOnlineRequest.getTxTime() + directRechargeOnlineRequest.getSeqNo());
        rechargeDetailLog.setResponseMessage(gson.toJson(directRechargeOnlineResponse));  // 响应吗
        rechargeDetailLogService.save(rechargeDetailLog);

        if (state == 1) {
            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(now, 60));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);

            return ResponseEntity.ok(VoBaseResp.ok("充值成功"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "充值失败！"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoUserAssetInfoResp> synOnLineRecharge(Long userId) throws Exception {
        Users users = userService.findByIdLock(userId);
        if(users.getIsLock()){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户已被系统锁定, 如有疑问请联系客服!", VoUserAssetInfoResp.class));
        }

        // 判断开户状态
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
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
        int pageSize = 20, pageIndex = 1, realSize =  0;
        String accountId = userThirdAccount.getAccountId();  // 存管账户ID
        Date nowDate = new Date() ;
        do {
            AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
            accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize)) ;
            accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex)) ;
            accountDetailsQueryRequest.setStartDate(DateHelper.getDate()) ; // 查询当天数据
            accountDetailsQueryRequest.setEndDate(DateHelper.getDate()) ;
            accountDetailsQueryRequest.setType("9");
            accountDetailsQueryRequest.setTranType("7820"); //  线下转账
            accountDetailsQueryRequest.setAccountId(accountId);

            AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                    accountDetailsQueryRequest,
                    AccountDetailsQueryResponse.class);

            if( (ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode())) ){
                String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg() ;
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, msg, VoUserAssetInfoResp.class));
            }

            String subPacks = accountDetailsQueryResponse.getSubPacks();
            if(StringUtils.isEmpty(subPacks)){
                break;
            }

            Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
            }.getType()));
            List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList());
            realSize = accountDetailsQueryItems.size();

            String seqNo ;
            for(AccountDetailsQueryItem accountDetailsQueryItem: accountDetailsQueryItems){   // 同步系统充值
                seqNo = String.format("%s%s%s", accountDetailsQueryItem.getInpDate(), accountDetailsQueryItem.getInpTime(), accountDetailsQueryItem.getTraceNo() ) ;
                RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findTopBySeqNo(seqNo);
                if(!ObjectUtils.isEmpty(rechargeDetailLog)){
                    continue;
                }
                Double money = new Double(accountDetailsQueryItem.getTxAmount()) * 100 ;

                // 根据对手
                // 插入该条记录
                rechargeDetailLog = new RechargeDetailLog() ;
                rechargeDetailLog.setUserId(users.getId());
                rechargeDetailLog.setBankName(userThirdAccount.getBankName());
                rechargeDetailLog.setCallbackTime(nowDate);
                rechargeDetailLog.setCreateTime(nowDate);
                rechargeDetailLog.setUpdateTime(nowDate);
                rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
                rechargeDetailLog.setDel(0);
                rechargeDetailLog.setState(1) ; // 充值成功
                rechargeDetailLog.setMoney(money.longValue());
                rechargeDetailLog.setRechargeChannel(1);  // 其他渠道
                rechargeDetailLog.setRechargeType(1); // 线下充值
                rechargeDetailLog.setSeqNo(seqNo);
                rechargeDetailLogService.save(rechargeDetailLog);

                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setMoney(money.intValue());
                entity.setType(CapitalChangeEnum.Recharge);
                entity.setUserId(userId);
                entity.setToUserId(userId);
                entity.setRemark("线下充值成功！");
                capitalChangeHelper.capitalChange(entity);

                // 触发用户充值
                MqConfig mqConfig = new MqConfig();
                mqConfig.setTag(MqTagEnum.RECHARGE);
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
                mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 30));
                ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
                mqConfig.setMsg(body);
                mqHelper.convertAndSend(mqConfig);
            }
        }while (realSize == pageSize) ;
        return userAssetInfo(userId);
    }

    @Override
    public ResponseEntity<VoHtmlResp> recharge(HttpServletRequest request, VoRechargeReq voRechargeReq) {
        Users users = userService.findById(voRechargeReq.getUserId());
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoHtmlResp.class));
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
        double[] rechargeCredit = bankAccountBiz.getCashCredit(voRechargeReq.getUserId());
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

            // 修改资金记录
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setMoney(money.intValue());
            entity.setType(CapitalChangeEnum.Recharge);
            entity.setUserId(userId);
            entity.setToUserId(userId);
            entity.setRemark("充值成功！");
            capitalChangeHelper.capitalChange(entity);

            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(now, 30));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
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
    public ResponseEntity<VoRechargeBankInfoResp> bankAcount(Long userId) {
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoRechargeBankInfoResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoRechargeBankInfoResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoRechargeBankInfoResp.class));
        }

        VoRechargeBankInfoResp voRechargeBankInfoResp = VoBaseResp.ok("查询成功!", VoRechargeBankInfoResp.class);
        voRechargeBankInfoResp.setBankCardNo(userThirdAccount.getAccountId());
        voRechargeBankInfoResp.setName(users.getRealname());
        voRechargeBankInfoResp.setBankName("江西银行");
        return ResponseEntity.ok(voRechargeBankInfoResp);
    }

    @Override
    public ResponseEntity<VoRechargeEntityWrapResp> log(Long userId, int pageIndex, int pageSize) {
        pageIndex = pageIndex <=0 ? 0 : pageIndex - 1 ;
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

    @Override
    public ResponseEntity<VoPreRechargeResp> preRecharge(Long userId) {
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoPreRechargeResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoPreRechargeResp.class));
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
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍后重试！", VoAssetIndexResp.class));
        }
        UserCache userCache = userCacheService.findById(userId);
        if (ObjectUtils.isEmpty(userCache)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍后重试！", VoAssetIndexResp.class));
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
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoAccruedMoneyResp.class));
        }

        Integer incomeBonus = userCache.getIncomeBonus();
        Integer incomeOverdue = userCache.getIncomeOverdue();
        Integer incomeInterest = userCache.getIncomeInterest();
        Integer incomeAward = userCache.getIncomeAward();
        Integer incomeIntegralCash = userCache.getIncomeIntegralCash();
        Integer incomeOther = userCache.getIncomeOther();
        Integer totalIncome = incomeBonus + incomeOverdue + incomeInterest + incomeAward + incomeIntegralCash + incomeOther;
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
    public ResponseEntity<VoAvailableAssetInfoResp> accountMoney(Long userId) {
        Asset asset = assetService.findByUserId(userId);

        if (ObjectUtils.isEmpty(asset)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoAvailableAssetInfoResp.class));
        }

        VoAvailableAssetInfoResp resp = VoBaseResp.ok("查询成功", VoAvailableAssetInfoResp.class);
        Integer noUserMoney = asset.getNoUseMoney();
        Integer userMoney = asset.getUseMoney();
        Integer total = (asset.getNoUseMoney() + asset.getUseMoney());

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
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoCollectionResp.class));
        }

        VoCollectionResp response = VoBaseResp.ok("查询成功", VoCollectionResp.class);

        Integer waitCollectionInterest = userCache.getWaitCollectionInterest();

        Integer waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Integer waitCollectionTotal = userCache.getWaitCollectionPrincipal() + userCache.getWaitCollectionInterest();
        response.setHideInterest(waitCollectionInterest);
        response.setInterest(StringHelper.formatMon(waitCollectionInterest / 100d));

        response.setPrincipal(StringHelper.formatMon(waitCollectionPrincipal / 100d));
        response.setHidePrincipal(waitCollectionPrincipal);

        response.setWaitCollectionTotal(StringHelper.formatMon(waitCollectionTotal / 100d));
        response.setHideWaitCollectionTotal(waitCollectionTotal);
        return ResponseEntity.ok(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> synchronizedAsset(Long userId) throws Exception {
        Users users = userService.findByIdLock(userId);
        if (ObjectUtils.isEmpty(users))
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "错误"));
        Boolean isLock = users.getIsLock();
        if (isLock) return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "错误"));

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount))
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "错误"));
        Asset asset = assetService.findByUserIdLock(userId);
        if (ObjectUtils.isEmpty(asset))
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "错误"));
        Date endDate = new Date();
        Date startDate = DateHelper.subDays(endDate, 3);
        int pageIndex = 1;
        int pageSize = 10;
        boolean looperState = true;
        Gson gson = new Gson();
        do {
            //  查询线下充值
            AccountDetailsQueryResponse response = doOffLineRecharge(pageIndex, pageSize, userThirdAccount.getAccountId(), startDate, endDate);
            if (ObjectUtils.isEmpty(response)) break;
            if (StringUtils.isEmpty(response.getSubPacks())) break;
            List<AccountDetailsQueryItem> accountDetailsQueryItems = gson.fromJson(response.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
            }.getType());
            if (CollectionUtils.isEmpty(accountDetailsQueryItems)) break;
            if (accountDetailsQueryItems.size() < 10) {
                looperState = false;
            }


            for (AccountDetailsQueryItem item : accountDetailsQueryItems) {
                String traceNo = item.getInpDate() + item.getInpTime() + item.getTraceNo();
                // 查询用户资金
                RechargeDetailLog record = rechargeDetailLogService.findTopBySeqNo(traceNo);
                if (!ObjectUtils.isEmpty(record)) {
                    break;
                }

                doOffLineAssetSynchronizedAsset(users, item, traceNo);
            }
            pageIndex++;
        } while (looperState);
        return ResponseEntity.ok(VoBaseResp.ok("成功"));
    }


    /**
     * 线下转账资金同步
     *
     * @param users
     * @param item
     * @param traceNo
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    private void doOffLineAssetSynchronizedAsset(Users users, AccountDetailsQueryItem item, String traceNo) throws Exception {
        Date now = new Date();
        // 添加重置记录
        RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
        rechargeDetailLog.setState(1); // 充值成功
        rechargeDetailLog.setUpdateTime(now);
        rechargeDetailLog.setCreateTime(now);
        rechargeDetailLog.setCallbackTime(now);
        rechargeDetailLog.setSeqNo(traceNo);
        rechargeDetailLog.setRechargeChannel(1); // 线下通道
        Double money = new Double(item.getTxAmount()) * 100;
        rechargeDetailLog.setMoney(money.longValue());
        rechargeDetailLog.setMobile(users.getPhone());
        rechargeDetailLog.setDel(0);
        rechargeDetailLog.setBankName("线下转账");
        rechargeDetailLog.setUserId(users.getId());
        rechargeDetailLog.setRechargeType(1);  // 线下充值
        rechargeDetailLog.setRechargeSource(4); // 充值
        rechargeDetailLog.setCardNo(item.getForAccountId());
        rechargeDetailLogService.save(rechargeDetailLog);
        // 资金变动
        CapitalChangeEntity capitalChangeEntity = new CapitalChangeEntity();
        capitalChangeEntity.setType(CapitalChangeEnum.Recharge);
        capitalChangeEntity.setUserId(users.getId());
        capitalChangeEntity.setRemark("线下充值成功");
        capitalChangeEntity.setMoney(money.intValue());
        capitalChangeEntity.setToUserId(users.getId());
        capitalChangeHelper.capitalChange(capitalChangeEntity);
        // 触发用户充值
        MqConfig mqConfig = new MqConfig();
        mqConfig.setTag(MqTagEnum.RECHARGE);
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
        mqConfig.setSendTime(DateHelper.addSeconds(now, 30));
        ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
        mqConfig.setMsg(body);
        mqHelper.convertAndSend(mqConfig);
    }

    /**
     * 查询线下充值
     *
     * @param pageIndex 下标
     * @param pageSize  页面
     * @param accountId 存管账户
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    private AccountDetailsQueryResponse doOffLineRecharge(int pageIndex, int pageSize, String accountId, Date startDate, Date endDate) {
        AccountDetailsQueryRequest request = new AccountDetailsQueryRequest();
        request.setAccountId(accountId);
        request.setStartDate(DateHelper.dateToString(startDate, DateHelper.DATE_FORMAT_YMD_NUM));
        request.setEndDate(DateHelper.dateToString(endDate, DateHelper.DATE_FORMAT_YMD_NUM));
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


}
