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
import com.gofobao.framework.api.model.direct_recharge_plus.auto_credit_invest_auth_plus.DirectRechargePlusRequest;
import com.gofobao.framework.api.model.direct_recharge_plus.auto_credit_invest_auth_plus.DirectRechargePlusResponse;
import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
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
import com.gofobao.framework.system.service.DictValueServcie;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    DictValueServcie dictValueServcie;

    @Autowired
    DictItemServcie dictItemServcie;


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

        VoUserAssetInfoResp voUserAssetInfoResp =VoBaseResp.ok("成功", VoUserAssetInfoResp.class) ;
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
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询成功", VoViewAssetLogWarpRes.class));
        }
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
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你还没有开通江西银行存管，请前往开通！", VoHtmlResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请初始化江西银行存管账户密码！", VoHtmlResp.class));
        }


        String srvTxCode = null;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.DIRECT_RECHARGE_PLUS, userThirdAccount.getMobile()), null);
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.DIRECT_RECHARGE_PLUS, userThirdAccount.getMobile()));
        } catch (Exception e) {
            log.error("UserThirdBizImpl autoTender get redis exception ", e);
        }

        if (StringUtils.isEmpty(srvTxCode)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取", VoHtmlResp.class));
        }

        DirectRechargePlusRequest directRechargePlusRequest = new DirectRechargePlusRequest();
        directRechargePlusRequest.setAccountId(userThirdAccount.getAccountId());
        directRechargePlusRequest.setIdType(IdTypeContant.ID_CARD);
        directRechargePlusRequest.setIdNo(userThirdAccount.getIdNo());
        directRechargePlusRequest.setName(users.getRealname());
        directRechargePlusRequest.setMobile(users.getPhone());
        directRechargePlusRequest.setCardNo(userThirdAccount.getCardNo());
        directRechargePlusRequest.setCurrency("156");
        directRechargePlusRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/asset/recharge/callback"));
        directRechargePlusRequest.setLastSrvAuthCode(srvTxCode);
        directRechargePlusRequest.setSmsCode(voRechargeReq.getSmsCode());
        directRechargePlusRequest.setAcqRes(users.getId().toString());
        directRechargePlusRequest.setChannel(ChannelContant.HTML);
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
        } catch (UnsupportedEncodingException e) {
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
                    .body("error");
        }

        if (!JixinResultContants.SUCCESS.equals(directRechargePlusResponse.getRetCode())) {
            log.error("AssetBizImpl.rechargeCallback: 回调出失败");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        Long userId = Long.parseLong(directRechargePlusResponse.getAcqRes());


        if (ObjectUtils.isEmpty(userId)) {
            log.error("AssetBizImpl.rechargeCallback:  userId is null");
            return ResponseEntity.badRequest().body("error");
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            log.error("AssetBizImpl.rechargeCallback: userThirdAccount is null");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        // 更改充值记录
        String seqNo = directRechargePlusResponse.getTxDate() + directRechargePlusResponse.getTxTime() + directRechargePlusResponse.getSeqNo();
        RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findTopBySeqNo(seqNo);
        if (ObjectUtils.isEmpty(rechargeDetailLog)) {
            log.error("AssetBizImpl.rechargeCallback: 没有该条充值记录");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        if (rechargeDetailLog.getState() == 1) {
            return ResponseEntity.ok("success");
        }

        if (!userId.equals(rechargeDetailLog.getUserId())) {
            log.error("AssetBizImpl.rechargeCallback: 当前充值不属于该用户");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        Double money = new Double(directRechargePlusResponse.getTxAmount()) * 100;
        // 验证金额
        if (rechargeDetailLog.getMoney() != money.longValue()) {
            log.error("AssetBizImpl.rechargeCallback: 充值金额不一致");
            return ResponseEntity
                    .badRequest()
                    .body("error");
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
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你还没有开通江西银行存管，请前往开通！", VoRechargeBankInfoResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请初始化江西银行存管账户密码！", VoRechargeBankInfoResp.class));
        }

        VoRechargeBankInfoResp voRechargeBankInfoResp = VoBaseResp.ok("查询成功!", VoRechargeBankInfoResp.class);
        voRechargeBankInfoResp.setBankCardNo(userThirdAccount.getAccountId());
        voRechargeBankInfoResp.setName(users.getRealname());
        voRechargeBankInfoResp.setBankName("江西银行");
        return ResponseEntity.ok(voRechargeBankInfoResp);
    }

    @Override
    public ResponseEntity<VoRechargeEntityWrapResp> log(Long userId, int pageIndex, int pageSize) {
        pageIndex = pageIndex < 0 ? 0 : pageIndex;
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
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你还没有开通江西银行存管，请前往开通！", VoPreRechargeResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请初始化江西银行存管账户密码！", VoPreRechargeResp.class));
        }

        VoPreRechargeResp voPreRechargeResp = VoBaseResp.ok("查询成功", VoPreRechargeResp.class);
        voPreRechargeResp.setBankName(userThirdAccount.getBankName());
        voPreRechargeResp.setCardNo(userThirdAccount.getCardNo().substring(userThirdAccount.getCardNo().length() - 4));
        voPreRechargeResp.setLogo(userThirdAccount.getBankLogo());
        DictValue bank = null;
        try {
            bank = bankLimitCache.get(userThirdAccount.getBankName());
        } catch (ExecutionException e) {
            log.error("AssetBizImpl.preRecharge: bank type is exists ");
        }

        if (ObjectUtils.isEmpty(bank)) {
            voPreRechargeResp.setTimesLimit("未知");
            voPreRechargeResp.setDayLimit("未知");
            voPreRechargeResp.setMouthLimit("未知");
        }
        // 返回银行信息
        voPreRechargeResp.setTimesLimit(bank.getValue04());
        voPreRechargeResp.setDayLimit(bank.getValue05());
        voPreRechargeResp.setMouthLimit(bank.getValue06());
        return ResponseEntity.ok(voPreRechargeResp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoPreCashResp> preCash(Long userId) {
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoPreCashResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你还没有开通江西银行存管，请前往开通！", VoPreCashResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请初始化江西银行存管账户密码！", VoPreCashResp.class));
        }

        Asset asset = assetService.findByUserIdLock(userId);
        VoPreCashResp resp = VoBaseResp.ok("查询成功", VoPreCashResp.class);
        resp.setBankName(userThirdAccount.getBankName());
        resp.setLogo(userThirdAccount.getBankLogo());
        resp.setCardNo(userThirdAccount.getCardNo());
        resp.setUseMoneyShow(StringHelper.formatDouble(asset.getUseMoney() / 100D, true));
        resp.setUseMoney(asset.getUseMoney() / 100D);

        // 获取用户免费额度

        // 获取用户资产

        //

        return null;
    }

    /**
     *  获取用户免费提现额度
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    private double getFreeCashMoney(long userId) {
        UserCache userCache = userCacheService.findByUserIdLock(userId);
        Asset asset = assetService.findByUserIdLock(userId);
        int canCashMoney = Math.min(asset.getUseMoney(), asset.getTotal() - asset.getPayment()) ;
        int allMoney = asset.getTotal() - asset.getPayment() - userCache.getWaitExpenditureInterestManageFee() ;

        // 充值总额
        long rechargeTotal = userCache.getRechargeTotal() ;
        // 已经实现收入
        int incomeTotal = userCache.getIncomeTotal() ;

        // 提现总额
        Long cashTotal = userCache.getCashTotal() ;

        Date endTime = new Date() ;
        Date startTime = DateHelper.subDays(endTime, 3) ;
        Long recharge3Total = 0L ;
        List<RechargeDetailLog> logs = rechargeDetailLogService.findByRecentLog(userId, 0, startTime, startTime) ;
        if(!CollectionUtils.isEmpty(logs)){
            recharge3Total = logs.stream().mapToLong(p->p.getMoney()).sum();
        }


        return 0d;
    }

    @Override
    public ResponseEntity<VoAssetIndexResp> asset(Long userId) {
        // 获取用户待还资金
        Asset asset = assetService.findByUserId(userId);
        if(ObjectUtils.isEmpty(asset)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍后重试！", VoAssetIndexResp.class));
        }
        UserCache userCache = userCacheService.findById(userId);
        if(ObjectUtils.isEmpty(userCache)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍后重试！", VoAssetIndexResp.class));
        }

        VoAssetIndexResp response = VoBaseResp.ok("查询成功", VoAssetIndexResp.class);
        response.setAccruedMoney( StringHelper.formatDouble(userCache.getIncomeTotal() / 100D, true)); //累计收益
        response.setCollectionMoney( StringHelper.formatDouble((userCache.getWaitCollectionPrincipal() + userCache.getWaitCollectionInterest()) / 100D, true)  ); // 待收
        response.setAccountMoney(StringHelper.formatDouble((asset.getNoUseMoney() + asset.getUseMoney()) / 100D , true));
        response.setTotalAsset(StringHelper.formatDouble((asset.getUseMoney() + asset.getNoUseMoney() + asset.getCollection()) / 100D, true));
        Double netAmount = ( (asset.getUseMoney() + userCache.getWaitCollectionPrincipal() ) * 0.8D - asset.getPayment())  / 100D;
        response.setNetAmount(StringHelper.formatDouble(netAmount, true));
        return ResponseEntity.ok(response) ;
    }

    @Override
    public ResponseEntity<VoAccruedMoneyResp> accruedMoney(Long userId) {
        UserCache userCache = userCacheService.findById(userId);
        if(ObjectUtils.isEmpty(userCache)){
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
        response.setIncomeBonus(StringHelper.formatDouble(incomeBonus / 100D, true));
        response.setIncomeAward(StringHelper.formatDouble(incomeAward / 100D, true));
        response.setIncomeInterest(StringHelper.formatDouble(incomeInterest / 100D, true));
        response.setIncomeIntegralCash(StringHelper.formatDouble(incomeIntegralCash / 100, true));
        response.setIncomeOther(StringHelper.formatDouble(incomeOther / 100D, true));
        response.setTotalIncome(StringHelper.formatDouble(totalIncome / 100 , true)) ;
        return ResponseEntity.ok(response) ;
    }

    @Override
    public ResponseEntity<VoAvailableAssetInfoResp> accountMoney(Long userId) {
        Asset asset = assetService.findByUserId(userId);

        if(ObjectUtils.isEmpty(asset)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoAvailableAssetInfoResp.class));
        }

        VoAvailableAssetInfoResp resp = VoBaseResp.ok("查询成功", VoAvailableAssetInfoResp.class);
        resp.setNoUseMoney(StringHelper.formatDouble(asset.getNoUseMoney() / 100D, true));
        resp.setUseMoney(StringHelper.formatDouble(asset.getUseMoney() / 100D, true));
        resp.setTotal(StringHelper.formatDouble((asset.getNoUseMoney() + asset.getUseMoney()) / 100D, true ));
        return ResponseEntity.ok(resp) ;
    }

    @Override
    public ResponseEntity<VoCollectionResp> collectionMoney(Long userId) {
        UserCache userCache = userCacheService.findById(userId);
        if(ObjectUtils.isEmpty(userCache)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoCollectionResp.class));
        }

        VoCollectionResp response = VoBaseResp.ok("查询成功", VoCollectionResp.class);
        response.setInterest(StringHelper.formatDouble(userCache.getWaitCollectionInterest() / 100D, true));
        response.setPrincipal(StringHelper.formatDouble(userCache.getWaitCollectionPrincipal() / 100D, true));
        response.setWaitCollectionTotal(StringHelper.formatDouble((userCache.getWaitCollectionPrincipal() + userCache.getWaitCollectionInterest()) / 100D, true));
        return ResponseEntity.ok(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> synchronizedAsset(Long userId) throws Exception{
        Users users = userService.findByIdLock(userId);
        if(ObjectUtils.isEmpty(users)) return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "错误")) ;
        Boolean isLock = users.getIsLock();
        if(isLock) return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "错误"));

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)) return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "错误"));
        Asset asset = assetService.findByUserIdLock(userId);
        if(ObjectUtils.isEmpty(asset)) return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "错误"));
        Date endDate = new Date();
        Date startDate =  DateHelper.subDays(endDate, 3) ;
        int pageIndex = 1 ;
        int pageSize = 10 ;
        boolean looperState = true;
        Gson gson = new Gson();
        do {
            //  查询线下充值
            AccountDetailsQueryResponse response = doOffLineRecharge(pageIndex, pageSize, userThirdAccount.getAccountId(), startDate, endDate);
            if(ObjectUtils.isEmpty(response)) break;
            if(StringUtils.isEmpty(response.getSubPacks())) break;
            List<AccountDetailsQueryItem> accountDetailsQueryItems = gson.fromJson(response.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>(){}.getType()) ;
            if(CollectionUtils.isEmpty(accountDetailsQueryItems)) break;
            if(accountDetailsQueryItems.size() < 10){
                looperState = false ;
            }


            for(AccountDetailsQueryItem item : accountDetailsQueryItems){
                String traceNo = item.getInpDate() + item.getInpTime() + item.getTraceNo();
                // 查询用户资金
                RechargeDetailLog record = rechargeDetailLogService.findTopBySeqNo(traceNo) ;
                if(!ObjectUtils.isEmpty(record)){
                    break;
                }

                doOffLineAssetSynchronizedAsset(users, item, traceNo);
            }
            pageIndex ++ ;
        }while (looperState) ;
        return ResponseEntity.ok(VoBaseResp.ok("成功")) ;
    }


    /**
     * 线下转账资金同步
     * @param users
     * @param item
     * @param traceNo
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    private void doOffLineAssetSynchronizedAsset(Users users, AccountDetailsQueryItem item, String traceNo) throws Exception {
        Date now = new Date() ;
        // 添加重置记录
        RechargeDetailLog rechargeDetailLog = new RechargeDetailLog() ;
        rechargeDetailLog.setState(1) ; // 充值成功
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
        rechargeDetailLog.setRechargeType(1) ;  // 线下充值
        rechargeDetailLog.setRechargeSource(4); // 充值
        rechargeDetailLog.setCardNo(item.getForAccountId());
        rechargeDetailLogService.save(rechargeDetailLog) ;
        // 资金变动
        CapitalChangeEntity capitalChangeEntity = new CapitalChangeEntity() ;
        capitalChangeEntity.setType(CapitalChangeEnum.Recharge);
        capitalChangeEntity.setUserId(users.getId());
        capitalChangeEntity.setRemark("线下充值成功");
        capitalChangeEntity.setMoney(money.intValue());
        capitalChangeEntity.setToUserId(users.getId());
        capitalChangeHelper.capitalChange(capitalChangeEntity) ;
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
     * @param pageIndex 下标
     * @param pageSize  页面
     * @param accountId 存管账户
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return
     */
    private AccountDetailsQueryResponse doOffLineRecharge(int pageIndex, int pageSize, String accountId, Date startDate, Date endDate ){
        AccountDetailsQueryRequest request = new AccountDetailsQueryRequest() ;
        request.setAccountId(accountId);
        request.setStartDate(DateHelper.dateToString(startDate, DateHelper.DATE_FORMAT_YMD_NUM));
        request.setEndDate(DateHelper.dateToString(endDate, DateHelper.DATE_FORMAT_YMD_NUM));
        request.setChannel(ChannelContant.HTML);
        request.setType("9"); // 转入
        request.setTranType("7820"); // 线下转账的
        request.setPageSize(String.valueOf(pageSize));
        request.setPageNum(String.valueOf(pageIndex));

        AccountDetailsQueryResponse response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY, request, AccountDetailsQueryResponse.class);
        if(ObjectUtils.isEmpty(response)){
            log.error(String.format("查询资金请求异常"));
            return null;
        }

        if(!JixinResultContants.SUCCESS.equals(response.getRetCode())){
            log.error(String.format("资金查询失败"));
            return null;
        }

        return response;
    }

}
