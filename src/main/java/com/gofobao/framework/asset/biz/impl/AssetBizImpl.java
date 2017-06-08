package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.contants.SrvTxCodeContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.direct_recharge_plus.auto_credit_invest_auth_plus.DirectRechargePlusRequest;
import com.gofobao.framework.api.model.direct_recharge_plus.auto_credit_invest_auth_plus.DirectRechargePlusResponse;
import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoAssetLog;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
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

        VoUserAssetInfoResp voUserAssetInfoResp = new VoUserAssetInfoResp();
        voUserAssetInfoResp.setUseMoney(useMoney);
        voUserAssetInfoResp.setNoUseMoney(asset.getNoUseMoney());
        voUserAssetInfoResp.setPayment(payment);
        voUserAssetInfoResp.setCollection(asset.getCollection());
        voUserAssetInfoResp.setNetWorthQuota(netWorthQuota);
        return ResponseEntity.ok(voUserAssetInfoResp);
    }

    /**
     * 账户流水
     *
     * @return
     */
    @Override
    public ResponseEntity<VoViewAssetLogWarpRes> assetLogResList(VoAssetLog voAssetLog) {
        try {
            List<VoViewAssetLogRes> resList = assetLogService.assetLogList(voAssetLog);
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


}
