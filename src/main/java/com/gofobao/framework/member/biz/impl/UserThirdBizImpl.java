package com.gofobao.framework.member.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.*;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.account_id_query.AccountIdQueryRequest;
import com.gofobao.framework.api.model.account_id_query.AccountIdQueryResponse;
import com.gofobao.framework.api.model.account_open.AccountOpenRequest;
import com.gofobao.framework.api.model.account_open.AccountOpenResponse;
import com.gofobao.framework.api.model.account_open_plus.AccountOpenPlusRequest;
import com.gofobao.framework.api.model.account_open_plus.AccountOpenPlusResponse;
import com.gofobao.framework.api.model.account_query_by_mobile.AccountQueryByMobileRequest;
import com.gofobao.framework.api.model.account_query_by_mobile.AccountQueryByMobileResponse;
import com.gofobao.framework.api.model.auto_bid_auth_plus.AutoBidAuthPlusRequest;
import com.gofobao.framework.api.model.auto_bid_auth_plus.AutoBidAuthPlusResponse;
import com.gofobao.framework.api.model.auto_credit_invest_auth.AutoCreditInvestAuthRequest;
import com.gofobao.framework.api.model.auto_credit_invest_auth.AutoCreditInvestAuthResponse;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.card_bind.CardBindRequest;
import com.gofobao.framework.api.model.card_bind.CardBindResponse;
import com.gofobao.framework.api.model.card_bind_details_query.CardBindDetailsQueryRequest;
import com.gofobao.framework.api.model.card_bind_details_query.CardBindDetailsQueryResponse;
import com.gofobao.framework.api.model.card_bind_details_query.CardBindItem;
import com.gofobao.framework.api.model.card_unbind.CardUnbindRequest;
import com.gofobao.framework.api.model.card_unbind.CardUnbindResponse;
import com.gofobao.framework.api.model.corpration_query.CorprationQueryRequest;
import com.gofobao.framework.api.model.corpration_query.CorprationQueryResponse;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryItem;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryRequest;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryResponse;
import com.gofobao.framework.api.model.password_reset.PasswordResetRequest;
import com.gofobao.framework.api.model.password_reset.PasswordResetResponse;
import com.gofobao.framework.api.model.password_set.PasswordSetRequest;
import com.gofobao.framework.api.model.password_set.PasswordSetResponse;
import com.gofobao.framework.api.model.password_set_query.PasswordSetQueryRequest;
import com.gofobao.framework.api.model.password_set_query.PasswordSetQueryResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.BankAccount;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.BankAccountService;
import com.gofobao.framework.borrow.vo.request.VoAdminModifyPasswordResp;
import com.gofobao.framework.borrow.vo.request.VoAdminOpenAccountResp;
import com.gofobao.framework.borrow.vo.request.VoPcDoFirstVerity;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.marketing.constans.MarketingTypeContants;
import com.gofobao.framework.marketing.entity.MarketingData;
import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.entity.UserInfo;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserInfoService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.request.UserAccountThirdTxReq;
import com.gofobao.framework.member.vo.request.VoOpenAccountReq;
import com.gofobao.framework.member.vo.response.*;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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

/**
 * Created by Max on 17/5/22.
 */
@Service
@Slf4j
@SuppressWarnings("all")
public class UserThirdBizImpl implements UserThirdBiz {
    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    UserService userService;

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    AssetService assetService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${gofobao.aliyun-bankinfo-url}")
    String aliyunQueryBankUrl;

    @Value("${gofobao.aliyun-bankinfo-appcode}")
    String aliyunQueryAppcode;

    @Autowired
    ThirdAccountPasswordHelper thirdAccountPasswordHelper;


    @Autowired
    DictValueService dictValueServcie;

    @Autowired
    DictItemService dictItemService;

    @Autowired
    BankBinHelper bankBinHelper;

    @Autowired
    private MqHelper mqHelper;

    @Autowired
    ThymeleafHelper thymeleafHelper;

    @Autowired
    OpenAccountBizImpl openAccountBiz;
    @Autowired
    private UserInfoService userInfoService;


    //用户来源
    private List<Integer> sources = Lists.newArrayList(0, 1, 2, 9);


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


    @Override
    public ResponseEntity<VoPreOpenAccountResp> preOpenAccount(Long userId) {
        //1。 验证用户是否存在
        Users user = userService.findById(userId);
        if (ObjectUtils.isEmpty(user)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你访问的账户不存在! ", VoPreOpenAccountResp.class));
        }

        //2. 判断用户是否已经开过存管账户
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
        if (!ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户已经开户！", VoPreOpenAccountResp.class));
        }

        // 4.查询银行卡
        List<BankAccount> bankAccountList = bankAccountService.listBankByUserId(userId);
        List<VoBankResp> voBankResps = new ArrayList<>(bankAccountList.size());
        VoBankResp voBankResp = null;
        for (BankAccount bankAccount : bankAccountList) {
            voBankResp = new VoBankResp();
            voBankResps.add(voBankResp);
            voBankResp.setId(bankAccount.getId());
            voBankResp.setBankNo(bankAccount.getAccount());
        }

        VoPreOpenAccountResp voPreOpenAccountResp = VoBaseResp.ok("查询成功", VoPreOpenAccountResp.class);
        voPreOpenAccountResp.setMobile(StringUtils.isEmpty(user.getPhone()) ? "" : user.getPhone());
        voPreOpenAccountResp.setIdType("01"); //证件类型：身份证
        voPreOpenAccountResp.setIdNo(ObjectUtils.isEmpty(user.getCardId()) ? "" : user.getCardId());
        voPreOpenAccountResp.setName(user.getRealname());
        voPreOpenAccountResp.setBankList(voBankResps);
        return ResponseEntity.ok(voPreOpenAccountResp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoOpenAccountResp> openAccount(VoOpenAccountReq voOpenAccountReq, Long userId, HttpServletRequest httpServletRequest) {
        voOpenAccountReq.setCardNo(voOpenAccountReq.getCardNo().toUpperCase());
        // 1.用户用户信息
        Users user = userService.findById(userId);
        if (ObjectUtils.isEmpty(user))
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你访问的账户不存在", VoOpenAccountResp.class));

        // 1.5 判断银行卡号唯一
        Specification<UserThirdAccount> userThirdAccountSpecification = Specifications.
                <UserThirdAccount>and()
                .eq("cardNo", voOpenAccountReq.getCardNo())
                .build();

        long cardNoCount = userThirdAccountService.count(userThirdAccountSpecification);
        if (cardNoCount > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "银行卡号已经开户", VoOpenAccountResp.class));
        }

        // 2. 判断用户是否已经开过存管账户
        UserThirdAccount userThirdAccount = null;
        try {
            userThirdAccount = queryUserThirdInfo(user.getId());
        } catch (Exception e) {
        }

        if (!ObjectUtils.isEmpty(userThirdAccount))
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户已经开户！", VoOpenAccountResp.class));

        ResponseEntity<VoOpenAccountResp> voOpenAccountRespResponseEntity = preCheckeForOpenAccount(voOpenAccountReq);
        if (!voOpenAccountRespResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return voOpenAccountRespResponseEntity;
        }

        String bankName = null;
        // 获取银行卡信息
        try {
            BankBinHelper.BankInfo bankInfo = bankBinHelper.find(voOpenAccountReq.getCardNo());

            if (ObjectUtils.isEmpty(bankInfo)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "查无此银行卡号, 如有问题请联系平台客服!", VoOpenAccountResp.class));
            }

            if (!bankInfo.getCardType().equals("借记卡")) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "银行卡类型必须为借记卡!", VoOpenAccountResp.class));
            }

            bankName = bankInfo.getBankName();
        } catch (Throwable e) {
            log.error("开户查询银行卡异常");
        }

        // 6 判断银行卡
        DictValue dictValue = null;
        try {
            dictValue = bankLimitCache.get(bankName);
        } catch (Throwable e) {
            log.error("查询平台支持银行异常", e);
        }
        if (ObjectUtils.isEmpty(dictValue)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("当前平台不支持%s", bankName), VoOpenAccountResp.class));
        }

        // 7.短信验证码验证
        String srvTxCode = null;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, voOpenAccountReq.getMobile()), null);
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, voOpenAccountReq.getMobile()));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl opeanAccountCallBack get redis exception ", e);
        }

        if (StringUtils.isEmpty(srvTxCode)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取", VoOpenAccountResp.class));
        }

        // 8.提交开户
        AccountOpenPlusRequest request = new AccountOpenPlusRequest();
        request.setIdType(IdTypeContant.ID_CARD);
        request.setName(voOpenAccountReq.getName());
        request.setMobile(voOpenAccountReq.getMobile());
        request.setIdNo(voOpenAccountReq.getIdNo());
        request.setAcctUse(AcctUseContant.GENERAL_ACCOUNT);
        request.setAcqRes(String.valueOf(user.getId()));
        request.setLastSrvAuthCode(srvTxCode);
        request.setChannel(ChannelContant.getchannel(httpServletRequest));
        request.setSmsCode(voOpenAccountReq.getSmsCode());
        request.setCardNo(voOpenAccountReq.getCardNo());

        AccountOpenPlusResponse response = jixinManager.send(JixinTxCodeEnum.OPEN_ACCOUNT_PLUS, request, AccountOpenPlusResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, msg, VoOpenAccountResp.class));
        }

        // 8.保存银行存管账户到用户中
        String accountId = response.getAccountId();
        UserThirdAccount entity = new UserThirdAccount();
        Date nowDate = new Date();
        entity.setUpdateAt(nowDate);
        entity.setUserId(user.getId());
        entity.setCreateAt(nowDate);
        entity.setCreateId(user.getId());
        entity.setUserId(user.getId());
        entity.setDel(0);
        entity.setMobile(voOpenAccountReq.getMobile());
        entity.setIdType(1);
        entity.setIdNo(voOpenAccountReq.getIdNo());
        entity.setCardNo(voOpenAccountReq.getCardNo());
        entity.setChannel(Integer.parseInt(ChannelContant.getchannel(httpServletRequest)));
        entity.setAcctUse(1);
        entity.setAccountId(accountId);
        entity.setPasswordState(0);
        entity.setCardNoBindState(1);
        entity.setName(voOpenAccountReq.getName());
        entity.setBankLogo(dictValue.getValue03());
        entity.setBankName(bankName);
        Long id = userThirdAccountService.save(entity);

        //  9.保存用户实名信息
        user.setRealname(voOpenAccountReq.getName());
        user.setCardId(voOpenAccountReq.getIdNo());
        user.setUpdatedAt(nowDate);
        userService.save(user);
        //10. 更新userInfo
        UserInfo userInfo = userInfoService.info(userId);
        String cardNo = voOpenAccountReq.getCardNo();
        int idxSexStart = 16;
        int birthYearSpan = 4;
        //如果是15位的证件号码
        if (cardNo.length() == 15) {
            idxSexStart = 14;
            birthYearSpan = 2;
        }
        //出生日期
        String year = (birthYearSpan == 2 ? "19" : "") + cardNo.substring(6, 6 + birthYearSpan);
        String month = cardNo.substring(6 + birthYearSpan, 6 + birthYearSpan + 2);
        String day = cardNo.substring(8 + birthYearSpan, 8 + birthYearSpan + 2);
        userInfo.setBirthdayY(NumberHelper.toInt(year));
        userInfo.setBirthdayMd(NumberHelper.toInt(month + day));
        userInfoService.save(userInfo);

        VoOpenAccountResp voOpenAccountResp = VoBaseResp.ok("开户成功", VoOpenAccountResp.class);
        voOpenAccountResp.setOpenAccountBankName("江西银行");
        voOpenAccountResp.setAccount(accountId);
        voOpenAccountResp.setName(voOpenAccountReq.getName());
        // 开户成功
        touchMarketingByOpenAccount(entity);
        return ResponseEntity.ok(voOpenAccountResp);
    }

    public static void main(String[] args) {

        String certificateNo = "360733199508260910";

        int idxSexStart = 16;
        int birthYearSpan = 4;
        //如果是15位的证件号码
        if (certificateNo.length() == 15) {
            idxSexStart = 14;
            birthYearSpan = 2;
        }

        //出生日期
        String year = (birthYearSpan == 2 ? "19" : "") + certificateNo.substring(6, 6 + birthYearSpan);
        String month = certificateNo.substring(6 + birthYearSpan, 6 + birthYearSpan + 2);
        String day = certificateNo.substring(8 + birthYearSpan, 8 + birthYearSpan + 2);
        String birthday = year + '-' + month + '-' + day;
        System.out.println(birthday);
    }

    /**
     * 开户前置检测
     *
     * @param voOpenAccountReq
     * @return
     */
    private ResponseEntity<VoOpenAccountResp> preCheckeForOpenAccount(VoOpenAccountReq voOpenAccountReq) {
        UserThirdAccount userThirdAccountbyMobile = userThirdAccountService.findByMobile(voOpenAccountReq.getMobile());  // 验证手机是否唯一
        if (!ObjectUtils.isEmpty(userThirdAccountbyMobile)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "手机已在存管平台开户, 无需开户！", VoOpenAccountResp.class));
        }

        // 从存管获取用户注册信息
        AccountQueryByMobileRequest accountQueryByMobileReques = new AccountQueryByMobileRequest();
        accountQueryByMobileReques.setMobile(voOpenAccountReq.getMobile());
        AccountQueryByMobileResponse accountQueryByMobileResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_QUERY_BY_MOBILE,
                accountQueryByMobileReques, AccountQueryByMobileResponse.class);
        if (!ObjectUtils.isEmpty(accountQueryByMobileResponse)
                && JixinResultContants.SUCCESS.equals(accountQueryByMobileResponse.getRetCode())
                && !StringUtils.isEmpty(accountQueryByMobileResponse.getAccountId())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "手机已在存管平台开户, 无需开户！", VoOpenAccountResp.class));
        }

        UserThirdAccount userThirdAccountByIdNo = userThirdAccountService.findByIdNo(voOpenAccountReq.getIdNo());
        if (!ObjectUtils.isEmpty(userThirdAccountByIdNo)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "身份证已在存管平台开户, 无需开户！", VoOpenAccountResp.class));
        }

        AccountIdQueryRequest accountIdQueryRequest = new AccountIdQueryRequest();
        accountIdQueryRequest.setIdNo(voOpenAccountReq.getIdNo());
        AccountIdQueryResponse accountIdQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_ID_QUERY,
                accountIdQueryRequest, AccountIdQueryResponse.class);
        if (!ObjectUtils.isEmpty(accountIdQueryResponse)
                && JixinResultContants.SUCCESS.equals(accountIdQueryResponse.getRetCode())
                && !StringUtils.isEmpty(accountIdQueryResponse.getAccountId())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "身份证已在存管平台开户, 无需开户！", VoOpenAccountResp.class));
        }

        return ResponseEntity.ok(VoBaseResp.error(VoBaseResp.OK, "查询成功", VoOpenAccountResp.class));
    }


    @Autowired
    MarketingRedpackRecordService marketingRedpackRecordService;


    /**
     * 触发开户活动
     *
     * @param userThirdAccount
     */
    private void touchMarketingByOpenAccount(UserThirdAccount userThirdAccount) {
        try {
            Long userId = userThirdAccount.getUserId();
            Users user = userService.findById(userId);
            Preconditions.checkNotNull(user, "touchMarketingByOpenAccount user is null");
            if (user.getIsLock()) {
                log.error("触发营销节点失败, 当前用户被冻结了");
                return;
            }

            Date nowDate = new Date();
            Long parentId = user.getParentId();
            if (!ObjectUtils.isEmpty(parentId)
                    && parentId > 0) {

                Users parentUser = userService.findById(parentId);
                if (ObjectUtils.isEmpty(parentId)) {
                    log.info("触发营销节点失败. 当前父类不存在");
                }

                if (parentUser.getIsLock()) {   // 父类被冻结, 直接冻结子类
                    // 冻结子类
                    user.setIsLock(true);
                    user.setUpdatedAt(nowDate);
                    userService.save(user);
                    return;
                }


                // 每天每个用户只能邀请100
                Specification<MarketingRedpackRecord> specifications = Specifications
                        .<MarketingRedpackRecord>and()
                        .eq("userId", parentId)
                        .between("publishTime", new Range<>(DateHelper.beginOfDate(nowDate), DateHelper.endOfDate(nowDate)))
                        .build();
                long count = marketingRedpackRecordService.count(specifications);
                if (count >= 10) {
                    // 触发一天超过个邀请用户
                    // 冻结他的父类和被邀请的子类
                    log.info("当天邀请注册数量超过10");
                    /*// 冻结父类
                    parentUser.setIsLock(true);
                    parentUser.setUpdatedAt(nowDate);
                    userService.save(parentUser);

                    // 冻结子类
                    user.setIsLock(true);
                    user.setUpdatedAt(nowDate);
                    userService.save(user);
                    log.error("此用户每天邀请好友过20, 系统拒绝执行");*/
                    return;
                }
            }
        } catch (Exception e) {
            log.error("邀请好友拦截异常", e);
        }

        MarketingData marketingData = new MarketingData();
        marketingData.setTransTime(DateHelper.dateToString(new Date()));
        marketingData.setUserId(userThirdAccount.getUserId().toString());
        marketingData.setSourceId(userThirdAccount.getId().toString());
        marketingData.setMarketingType(MarketingTypeContants.OPEN_ACCOUNT);
        Gson gson = new Gson();
        try {
            String json = gson.toJson(marketingData);
            Map<String, String> data = gson.fromJson(json, TypeTokenContants.MAP_ALL_STRING_TOKEN);
            MqConfig mqConfig = new MqConfig();
            mqConfig.setMsg(data);
            mqConfig.setTag(MqTagEnum.MARKETING_OPEN_ACCOUNT);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_MARKETING);
            mqHelper.convertAndSend(mqConfig);
            log.info(String.format("开户营销节点触发: %s", new Gson().toJson(marketingData)));
        } catch (Throwable e) {
            log.error(String.format("开户营销节点触发异常：%s", new Gson().toJson(marketingData)), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoHtmlResp> modifyOpenAccPwd(HttpServletRequest httpServletRequest, Long userId) {
        UserThirdAccount userThirdAccount = null;
        try {
            userThirdAccount = queryUserThirdInfo(userId);
        } catch (Exception e) {
        }
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通!", VoHtmlResp.class));
        }

        String html = null;
        // 判断用户是密码初始化还是
        html = generateModifyPasswordHtml(httpServletRequest, userId, userThirdAccount);
        if (StringUtils.isEmpty(html)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }


        VoHtmlResp voHtmlResp = VoBaseResp.ok("成功", VoHtmlResp.class);
        try {
            voHtmlResp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl modifyOpenAccPwd gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }

        return ResponseEntity.ok(voHtmlResp);
    }

    @Override
    public ResponseEntity<String> modifyOpenAccPwdCallback(HttpServletRequest request, HttpServletResponse response, Integer type) {
        Long userId = null;

        if (type == 1) {
            PasswordSetResponse passwordSetResponse = jixinManager.callback(request, new TypeToken<PasswordSetResponse>() {
            });

            if (ObjectUtils.isEmpty(passwordSetResponse)) {
                return ResponseEntity.badRequest().body("success");
            }
            if (!JixinResultContants.SUCCESS.equals(passwordSetResponse.getRetCode())) {
                log.error("UserThirdBizImpl.modifyOpenAccPwdCallback: 回调出失败");
                return ResponseEntity
                        .badRequest()
                        .body("success");
            }

            userId = Long.parseLong(passwordSetResponse.getAcqRes());
        } else {
            PasswordResetResponse passwordResetResponse = jixinManager.callback(request, new TypeToken<PasswordResetResponse>() {
            });

            if (ObjectUtils.isEmpty(passwordResetResponse)) {
                return ResponseEntity.badRequest().body("success");
            }

            if (!JixinResultContants.SUCCESS.equals(passwordResetResponse.getRetCode())) {
                log.error("UserThirdBizImpl.modifyOpenAccPwdCallback: 回调出失败");
                return ResponseEntity
                        .badRequest()
                        .body("success");
            }
            userId = Long.parseLong(passwordResetResponse.getAcqRes());
        }


        if (ObjectUtils.isEmpty(userId)) {
            log.error("UserThirdBizImpl modifyOpenAccPwdCallback userId is null");
            return ResponseEntity.badRequest().body("success");
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            log.error("UserThirdBizImpl modifyOpenAccPwdCallback userThirdAccount is null");
            return ResponseEntity.badRequest().body("success");
        }

        if (userThirdAccount.getPasswordState().equals(1)) {
            return ResponseEntity.ok("success");
        }

        userThirdAccount.setPasswordState(1);
        userThirdAccount.setUpdateAt(new Date());
        Long id = userThirdAccountService.save(userThirdAccount);
        if (id == 0) {
            log.error("UserThirdBizImpl modifyOpenAccPwdCallback update userThirdAccount is error");
            return ResponseEntity.badRequest().body("success");
        }

        return ResponseEntity.ok("success");
    }

    @Override
    public ResponseEntity<String> autoTenderCallback(HttpServletRequest request, HttpServletResponse response) {
        AutoBidAuthPlusResponse autoBidAuthResponse = jixinManager.callback(request, new TypeToken<AutoBidAuthPlusResponse>() {
        });

        if (ObjectUtils.isEmpty(autoBidAuthResponse)) {
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        if (!JixinResultContants.SUCCESS.equals(autoBidAuthResponse.getRetCode())) {
            log.error("UserThirdBizImpl.autoTenderCallback: 回调出失败");
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        Long userId = Long.parseLong(autoBidAuthResponse.getAcqRes());


        if (ObjectUtils.isEmpty(userId)) {
            log.error("UserThirdBizImpl autoTenderCallback userId is null");
            return ResponseEntity.badRequest().body("success");
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            log.error("UserThirdBizImpl autoTenderCallback userThirdAccount is null");
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }


        if (userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity.ok("success");
        }

        userThirdAccount.setAutoTenderState(1);
        userThirdAccount.setAutoTenderTotAmount(999999999L);
        userThirdAccount.setAutoTenderTxAmount(999999999L);
        userThirdAccount.setAutoTenderOrderId(autoBidAuthResponse.getOrderId());
        userThirdAccount.setUpdateAt(new Date());
        Long id = userThirdAccountService.save(userThirdAccount);
        if (id == 0) {
            log.error("UserThirdBizImpl autoTenderCallback update userThirdAccount is error");
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        return ResponseEntity.ok("success");
    }

    @Override
    public ResponseEntity<VoHtmlResp> autoTender(HttpServletRequest httpServletRequest, Long userId) {
        String html = null;
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先开通江西银行存管账户！", VoHtmlResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先设置江西银行存管账户交易密码！", VoHtmlResp.class));
        }

        if (userThirdAccount.getAutoTenderState().equals(0)) {
            log.info("查询用户签约状态开始");
            CreditAuthQueryRequest creditAuthQueryRequest = new CreditAuthQueryRequest();
            creditAuthQueryRequest.setAccountId(userThirdAccount.getAccountId());
            creditAuthQueryRequest.setType("1");
            creditAuthQueryRequest.setChannel(ChannelContant.APP);
            CreditAuthQueryResponse creditAuthQueryResponse = jixinManager
                    .send(JixinTxCodeEnum.CREDIT_AUTH_QUERY, creditAuthQueryRequest, CreditAuthQueryResponse.class);
            if ((!ObjectUtils.isEmpty(creditAuthQueryResponse)) && (creditAuthQueryResponse.getRetCode().equalsIgnoreCase(JixinResultContants.SUCCESS))) {
                if (creditAuthQueryResponse.getState().equalsIgnoreCase("1")) {
                    UserThirdAccount dbEntity = userThirdAccountService.findByUserId(userThirdAccount.getUserId());
                    dbEntity.setUpdateAt(new Date());
                    dbEntity.setAutoTenderState(1);
                    dbEntity.setAutoTenderOrderId(creditAuthQueryResponse.getOrderId());
                    dbEntity.setAutoTenderTotAmount(999999999L);
                    dbEntity.setAutoTenderTxAmount(999999999L);
                    userThirdAccountService.save(dbEntity);
                    return ResponseEntity
                            .badRequest()
                            .body(VoBaseResp.error(VoBaseResp.ERROR, "你已签约自动投标协议！", VoHtmlResp.class));
                }
            }
            log.info("查询用户签约状态结束");
        }


        AutoBidAuthPlusRequest autoBidAuthRequest = new AutoBidAuthPlusRequest();
        autoBidAuthRequest.setAccountId(userThirdAccount.getAccountId());
        autoBidAuthRequest.setOrderId(System.currentTimeMillis() + RandomHelper.generateNumberCode(6));
        autoBidAuthRequest.setTxAmount("999999999");
        autoBidAuthRequest.setTotAmount("999999999");
        autoBidAuthRequest.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userId));
        //autoBidAuthRequest.setRetUrl(String.format("%s%s%s", javaDomain, "/pub/autoTender/show/", userId));
        autoBidAuthRequest.setRetUrl(String.format("%s/pub/openAccount/callback/%s/autoTender", javaDomain, userThirdAccount.getUserId()));
        autoBidAuthRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/user/third/autoTender/callback"));
        autoBidAuthRequest.setAcqRes(userId.toString());
        autoBidAuthRequest.setChannel(ChannelContant.getchannel(httpServletRequest));

        try {
            html = jixinManager.getHtml(JixinTxCodeEnum.AUTO_BID_AUTH, autoBidAuthRequest);
        } catch (Throwable e) {
            log.error("UserThirdBizImpl autoTender get redis exception ", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }

        VoHtmlResp resp = VoBaseResp.ok("请求成功", VoHtmlResp.class);
        try {
            resp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl autoTender gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<VoHtmlResp> autoTranfter(HttpServletRequest httpServletRequest, Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先开通江西银行存管账户！", VoHtmlResp.class));
        }

        if (!userThirdAccount.getPasswordState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先设置江西银行存管账户交易密码！", VoHtmlResp.class));
        }

        if (!userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先签约江西银行自动投标协议！", VoHtmlResp.class));
        }

        if (userThirdAccount.getAutoTransferState().equals(0)) {  // 审核过
            log.info("查询用户自动债权转让协议开始");
            CreditAuthQueryRequest creditAuthQueryRequest = new CreditAuthQueryRequest();
            creditAuthQueryRequest.setAccountId(userThirdAccount.getAccountId());
            creditAuthQueryRequest.setType("2");
            creditAuthQueryRequest.setChannel(ChannelContant.APP);
            CreditAuthQueryResponse creditAuthQueryResponse = jixinManager
                    .send(JixinTxCodeEnum.CREDIT_AUTH_QUERY, creditAuthQueryRequest, CreditAuthQueryResponse.class);
            if ((!ObjectUtils.isEmpty(creditAuthQueryResponse)) && (creditAuthQueryResponse.getRetCode().equalsIgnoreCase(JixinResultContants.SUCCESS))) {
                if (creditAuthQueryResponse.getState().equalsIgnoreCase("1")) {
                    UserThirdAccount dbEntity = userThirdAccountService.findByUserId(userThirdAccount.getUserId());
                    dbEntity.setUpdateAt(new Date());
                    dbEntity.setAutoTransferState(1);
                    dbEntity.setAutoTransferBondOrderId(creditAuthQueryResponse.getOrderId());
                    userThirdAccountService.save(dbEntity);
                    return ResponseEntity
                            .badRequest()
                            .body(VoBaseResp.error(VoBaseResp.ERROR, "你已经签署自动债权转让协议！", VoHtmlResp.class));
                }
            }
            log.info("查询用户自动债权转让协议结束");
        }
        AutoCreditInvestAuthRequest autoCreditInvestAuthRequest = new AutoCreditInvestAuthRequest();
        autoCreditInvestAuthRequest.setAccountId(userThirdAccount.getAccountId());
        autoCreditInvestAuthRequest.setOrderId(System.currentTimeMillis() + RandomHelper.generateNumberCode(6));
        autoCreditInvestAuthRequest.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userId));
        // autoCreditInvestAuthPlusRequest.setRetUrl(String.format("%s%s%s", javaDomain, "/pub/autoTranfer/show/", userId));
        autoCreditInvestAuthRequest.setRetUrl(String.format("%s/pub/openAccount/callback/%s/autoTranfer", javaDomain, userThirdAccount.getUserId()));
        autoCreditInvestAuthRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/user/third/autoTranfer/callback"));
        autoCreditInvestAuthRequest.setAcqRes(userId.toString());
        autoCreditInvestAuthRequest.setChannel(ChannelContant.getchannel(httpServletRequest));


        String html = null;
        try {
            html = jixinManager.getHtml(JixinTxCodeEnum.AUTO_CREDIT_INVEST_AUTH, autoCreditInvestAuthRequest);
        } catch (Throwable e) {
            log.error("UserThirdBizImpl autoTranfter get redis exception ", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }

        VoHtmlResp resp = VoBaseResp.ok("请求成功", VoHtmlResp.class);
        try {
            resp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl autoTender autoTranfter exception", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<String> autoTranferCallback(HttpServletRequest request, HttpServletResponse response) {
        AutoCreditInvestAuthResponse autoCreditInvestAuthResponse = jixinManager.callback(request, new TypeToken<AutoCreditInvestAuthResponse>() {
        });

        if (ObjectUtils.isEmpty(autoCreditInvestAuthResponse)) {
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        if (!JixinResultContants.SUCCESS.equals(autoCreditInvestAuthResponse.getRetCode())) {
            log.error("UserThirdBizImpl.autoTranferCallback: 回调出失败");
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        Long userId = Long.parseLong(autoCreditInvestAuthResponse.getAcqRes());

        if (ObjectUtils.isEmpty(userId)) {
            log.error("UserThirdBizImpl autoTranferCallback userId is null");
            return ResponseEntity.badRequest().body("success");
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            log.error("UserThirdBizImpl autoTranferCallback userThirdAccount is null");
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }


        if (userThirdAccount.getAutoTransferState().equals(1)) {  // 审核
            return ResponseEntity.ok("success");
        }

        userThirdAccount.setAutoTransferState(1);
        userThirdAccount.setAutoTransferBondOrderId(autoCreditInvestAuthResponse.getOrderId());
        userThirdAccount.setUpdateAt(new Date());
        Long id = userThirdAccountService.save(userThirdAccount);
        if (id == 0) {
            log.error("UserThirdBizImpl autoTranferCallback update userThirdAccount is error");
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        return ResponseEntity.ok("success");
    }

    @Override
    public ResponseEntity<VoSignInfoResp> querySigned(Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先开通江西银行存管账户！", VoSignInfoResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先设置江西银行存管账户交易密码！", VoSignInfoResp.class));
        }


        userThirdAccount = synCreditQuth(userThirdAccount);
        VoSignInfoResp re = VoBaseResp.ok("查询成功", VoSignInfoResp.class);
        re.setAutoTenderState(userThirdAccount.getAutoTenderState().equals(1));
        re.setAutoTransferState(userThirdAccount.getAutoTransferState().equals(1));  // 审核
        return ResponseEntity.ok(re);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String showPassword(Long id, Model model) {
        UserThirdAccount userThirdAccount = null;
        try {
            userThirdAccount = queryUserThirdInfo(id);
        } catch (Exception e) {
            userThirdAccount = null;
        }
        model.addAttribute("h5Domain", h5Domain);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return "password/faile";
        }

        if (userThirdAccount.getPasswordState() == 1) {
            return "password/success";
        } else {
            return "password/faile";
        }
    }


    @Override
    public String thirdAccountProtocolJson(Long userId) {
        Users users = userService.findById(userId);
        String username = users.getUsername();
        if (StringUtils.isEmpty(username)) {
            username = users.getPhone();
        }
        if (StringUtils.isEmpty(username)) {
            username = users.getEmail();
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("customerName", username);
        paramMap.put("playformName", "深圳市广富宝金融信息服务有限公司");
        return thymeleafHelper.build("thirdAccount/thirdAccountProtocol", paramMap);
    }

    @Override
    public void thirdAccountProtocol(Long userId, Model model) {
        Users users = userService.findById(userId);
        String username = users.getUsername();
        if (StringUtils.isEmpty(username)) {
            username = users.getPhone();
        }
        if (StringUtils.isEmpty(username)) {
            username = users.getEmail();
        }

        model.addAttribute("customerName", username);
        model.addAttribute("playformName", "深圳市广富宝金融信息服务有限公司");
    }

    @Override
    public String showAutoTender(Long id, Model model) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(id);
        model.addAttribute("h5Domain", h5Domain);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return "autoTender/faile";
        }

        if (userThirdAccount.getAutoTenderState().equals(1)) {
            return "autoTender/success";
        } else {
            return "autoTender/faile";
        }
    }

    @Override
    public String showAutoTranfer(Long id, Model model) {

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(id);
        model.addAttribute("h5Domain", h5Domain);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return "autoTranfer/faile";
        }

        if (userThirdAccount.getAutoTransferState().equals(1)) {  // 审核
            return "autoTranfer/success";
        } else {
            return "autoTranfer/faile";
        }
    }
    @Override
    public ResponseEntity<String> publicPasswordModify(HttpServletRequest httpServletRequest, String encode, String channel) {
        Long userId = thirdAccountPasswordHelper.getUserId(encode);
        if (userId == null) {
            throw new RuntimeException("非法请求");
        }
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            throw new RuntimeException("设置密码");
        }
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setMobile(userThirdAccount.getMobile());
        passwordResetRequest.setChannel(channel);
        passwordResetRequest.setName(userThirdAccount.getName());
        passwordResetRequest.setAccountId(userThirdAccount.getAccountId());
        passwordResetRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        passwordResetRequest.setIdNo(userThirdAccount.getIdNo());
        passwordResetRequest.setIdNo(userThirdAccount.getIdNo());
        passwordResetRequest.setAcqRes(String.valueOf(userId));
        passwordResetRequest.setRetUrl(String.format("%s%s/%s", javaDomain, "/pub/password/show", userId));
        passwordResetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/2"));
        String html = jixinManager.getHtml(JixinTxCodeEnum.PASSWORD_RESET, passwordResetRequest);
        return ResponseEntity.ok(html);
    }

    @Override
    public UserThirdAccount synCreditQuth(UserThirdAccount userThirdAccount) {
        if (userThirdAccount.getAutoTenderState().equals(0)) {
            CreditAuthQueryRequest creditAuthQueryRequest = new CreditAuthQueryRequest();
            creditAuthQueryRequest.setAccountId(userThirdAccount.getAccountId());
            creditAuthQueryRequest.setType("1");
            creditAuthQueryRequest.setChannel(ChannelContant.APP);
            CreditAuthQueryResponse creditAuthQueryResponse = jixinManager
                    .send(JixinTxCodeEnum.CREDIT_AUTH_QUERY, creditAuthQueryRequest, CreditAuthQueryResponse.class);

            if ((!ObjectUtils.isEmpty(creditAuthQueryResponse)) && (creditAuthQueryResponse.getRetCode().equalsIgnoreCase(JixinResultContants.SUCCESS))) {
                if (creditAuthQueryResponse.getState().equalsIgnoreCase("1")) {
                    // 同步信息
                    UserThirdAccount dbEntity = userThirdAccountService.findByUserId(userThirdAccount.getUserId());
                    dbEntity.setUpdateAt(new Date());
                    dbEntity.setAutoTenderState(1);
                    dbEntity.setAutoTenderOrderId(creditAuthQueryResponse.getOrderId());
                    dbEntity.setAutoTenderTotAmount(999999999L);
                    dbEntity.setAutoTenderTxAmount(999999999L);
                    userThirdAccountService.save(dbEntity);
                }
            }
        }

        if (userThirdAccount.getAutoTransferState().equals(0)) { // 审核
            CreditAuthQueryRequest creditAuthQueryRequest = new CreditAuthQueryRequest();
            creditAuthQueryRequest.setAccountId(userThirdAccount.getAccountId());
            creditAuthQueryRequest.setType("2");
            creditAuthQueryRequest.setChannel(ChannelContant.APP);
            CreditAuthQueryResponse creditAuthQueryResponse = jixinManager
                    .send(JixinTxCodeEnum.CREDIT_AUTH_QUERY, creditAuthQueryRequest, CreditAuthQueryResponse.class);
            if ((!ObjectUtils.isEmpty(creditAuthQueryResponse)) && (creditAuthQueryResponse.getRetCode().equalsIgnoreCase(JixinResultContants.SUCCESS))) {
                if (creditAuthQueryResponse.getState().equalsIgnoreCase("1")) {
                    // 同步信息
                    UserThirdAccount dbEntity = userThirdAccountService.findByUserId(userThirdAccount.getUserId());
                    dbEntity.setUpdateAt(new Date());
                    dbEntity.setAutoTransferState(1);
                    dbEntity.setAutoTransferBondOrderId(creditAuthQueryResponse.getOrderId());
                    userThirdAccountService.save(dbEntity);
                }
            }
        }

        return userThirdAccountService.findByUserId(userThirdAccount.getUserId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoHtmlResp> adminOpenAccount(VoAdminOpenAccountResp voAdminOpenAccountResp, HttpServletRequest httpServletRequest) {
        if (!SecurityHelper.checkSign(voAdminOpenAccountResp.getSign(), voAdminOpenAccountResp.getParamStr())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "开户请求参数非法", VoHtmlResp.class));
        }

        // 开户主体信息, phone: 开户手机. name: 用户名称, userId: 用户ID, idNo:身份证号, cardNo: 银行卡号;
        Map<String, String> openAccountBodyMap = new Gson().fromJson(voAdminOpenAccountResp.getParamStr(), TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String phone = openAccountBodyMap.get("phone");
        Long userId = Long.parseLong(openAccountBodyMap.get("userId"));
        String realMame = openAccountBodyMap.get("name");
        String idNo = openAccountBodyMap.get("idNo");
        String cardNo = openAccountBodyMap.get("cardNo");

        // 1.用户用户信息
        Users user = userService.findById(userId);
        if (ObjectUtils.isEmpty(user))
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你访问的账户不存在", VoHtmlResp.class));
        // 2. 判断用户是否已经开过存管账户
        UserThirdAccount userThirdAccount = null;
        try {
            userThirdAccount = queryUserThirdInfo(user.getId());
        } catch (Exception e) {
        }

        if (!ObjectUtils.isEmpty(userThirdAccount))
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户已经开户！", VoHtmlResp.class));

        UserThirdAccount userThirdAccountbyMobile = userThirdAccountService.findByMobile(phone);
        if (!ObjectUtils.isEmpty(userThirdAccountbyMobile)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "手机已在存管平台开户, 无需开户！", VoHtmlResp.class));
        }

        String bankName = null;
        // 获取银行卡信息
        try {
            BankBinHelper.BankInfo bankInfo = bankBinHelper.find(cardNo);

            if (ObjectUtils.isEmpty(bankInfo)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "查无此银行卡号, 如有问题请联系平台客服!", VoHtmlResp.class));
            }

            if (!bankInfo.getCardType().equals("借记卡")) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "银行卡类型必须为借记卡!", VoHtmlResp.class));
            }

            bankName = bankInfo.getBankName();
        } catch (Throwable e) {
            log.error("开户查询银行卡异常");
        }

        // 6 判断银行卡
        DictValue dictValue = null;
        try {
            dictValue = bankLimitCache.get(bankName);
        } catch (Throwable e) {
            log.error("查询平台支持银行异常", e);
        }
        if (ObjectUtils.isEmpty(dictValue)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("当前平台不支持%s", bankName), VoHtmlResp.class));
        }


        // 8.提交开户
        AccountOpenRequest accountOpenRequest = new AccountOpenRequest();
        accountOpenRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        accountOpenRequest.setIdNo(idNo);
        accountOpenRequest.setName(realMame);
        accountOpenRequest.setMobile(phone);
        accountOpenRequest.setCardNo(cardNo);
        accountOpenRequest.setRetUrl(thirdAccountPasswordHelper.getThirdAcccountInitPasswordUrl(httpServletRequest, userId)); // 初始化密码
        accountOpenRequest.setNotifyUrl(String.format("%s%s/%s", javaDomain, "/pub/admin/third/openAccout/callback", userId)); // 后台通知
        accountOpenRequest.setAcctUse(AcctUseContant.GENERAL_ACCOUNT);
        accountOpenRequest.setAcqRes(String.valueOf(user.getId()));
        accountOpenRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
        String html = jixinManager.getHtml(JixinTxCodeEnum.OPEN_ACCOUNT, accountOpenRequest);
        VoHtmlResp voHtmlResp = VoBaseResp.ok("操作成功", VoHtmlResp.class);
        try {
            voHtmlResp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了, 请稍后重试!", VoHtmlResp.class));
        }


        // 8.保存银行存管账户到用户中
        UserThirdAccount entity = userThirdAccountService.findByDelUseid(userId);  // 防止重复开户
        if (ObjectUtils.isEmpty(entity)) {
            entity = new UserThirdAccount();
        }
        Date nowDate = new Date();
        entity.setUpdateAt(nowDate);
        entity.setUserId(user.getId());
        entity.setCreateAt(nowDate);
        entity.setCreateId(user.getId());
        entity.setUserId(user.getId());
        entity.setDel(1);
        entity.setMobile(phone);
        entity.setIdType(1);
        entity.setIdNo(idNo);
        entity.setCardNo(cardNo);
        entity.setChannel(Integer.parseInt(ChannelContant.getchannel(httpServletRequest)));
        entity.setAcctUse(1);
        entity.setAccountId("");
        entity.setPasswordState(0);
        entity.setCardNoBindState(1);
        entity.setName(realMame);
        entity.setBankLogo(dictValue.getValue03());
        entity.setBankName(bankName);
        userThirdAccountService.save(entity);
        return ResponseEntity.ok(voHtmlResp);
    }

    @Override
    public ResponseEntity<String> adminOpenAccountCallback(HttpServletRequest httpServletRequest, Long userId) {
        AccountOpenResponse accountOpenResponse = jixinManager.callback(httpServletRequest, new TypeToken<AccountOpenResponse>() {
        });

        if (ObjectUtils.isEmpty(accountOpenResponse)) {
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        if (!JixinResultContants.SUCCESS.equals(accountOpenResponse.getRetCode())) {
            log.error("UserThirdBizImpl.adminOpenAccountCallback: 回调出失败");


            userThirdAccountService.deleteByUserId(userId);
            return ResponseEntity
                    .badRequest()
                    .body("success");
        }

        Date nowDate = new Date();
        UserThirdAccount userThirdAccount = userThirdAccountService.findByDelUseid(userId);
        userThirdAccount.setAccountId(accountOpenResponse.getAccountId());
        userThirdAccount.setUpdateAt(new Date());
        userThirdAccount.setDel(0);
        userThirdAccountService.save(userThirdAccount);

        Users user = userService.findById(userId);
        user.setRealname(userThirdAccount.getName());
        user.setCardId(userThirdAccount.getIdNo());
        user.setUpdatedAt(nowDate);
        userService.save(user);

        return ResponseEntity.ok("success");
    }

    @Override
    public ResponseEntity<String> adminPasswordInitCallback(HttpServletRequest request, HttpServletResponse response, Integer type) {
        PasswordSetResponse passwordSetResponse = jixinManager.callback(request, new TypeToken<PasswordSetResponse>() {
        });

        if (ObjectUtils.isEmpty(passwordSetResponse)) {
            return ResponseEntity.badRequest().body("success");
        }
        if (!JixinResultContants.SUCCESS.equals(passwordSetResponse.getRetCode())) {
            log.error("UserThirdBizImpl.adminPasswordInitCallback: 回调出失败");
            return ResponseEntity.ok("success");
        }

        Long userId = Long.parseLong(passwordSetResponse.getAcqRes());

        if (ObjectUtils.isEmpty(userId)) {
            log.error("UserThirdBizImpl adminPasswordInitCallback userId is null");
            return ResponseEntity.badRequest().body("success");
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            log.error("UserThirdBizImpl adminPasswordInitCallback userThirdAccount is null");
            return ResponseEntity.badRequest().body("success");
        }

        if (userThirdAccount.getPasswordState().equals(1)) {
            return ResponseEntity.ok("success");
        }

        userThirdAccount.setPasswordState(1);
        userThirdAccount.setUpdateAt(new Date());
        Long id = userThirdAccountService.save(userThirdAccount);
        if (id == 0) {
            log.error("UserThirdBizImpl adminPasswordInitCallback update userThirdAccount is error");
            return ResponseEntity.badRequest().body("success");
        }

        return ResponseEntity.ok("success");
    }

    @Override
    public ResponseEntity<String> adminPasswordInit(HttpServletRequest httpServletRequest, String encode, String channel) {
        Long userId = thirdAccountPasswordHelper.getUserId(encode);
        if (userId == null) {
            throw new RuntimeException("adminPasswordInit. 非法请求");
        }
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);

        // 提示用户开户不成功
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity.ok("开户失败!");
        }
        PasswordSetRequest passwordSetRequest = new PasswordSetRequest();
        passwordSetRequest.setMobile(userThirdAccount.getMobile());
        passwordSetRequest.setChannel(channel);
        passwordSetRequest.setName(userThirdAccount.getName());
        passwordSetRequest.setAccountId(userThirdAccount.getAccountId());
        passwordSetRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        passwordSetRequest.setIdNo(userThirdAccount.getIdNo());
        passwordSetRequest.setAcqRes(String.valueOf(userId));
        passwordSetRequest.setRetUrl(String.format("%s%s/%s", javaDomain, "/pub/password/show", userId));
        passwordSetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/1"));
        String html = jixinManager.getHtml(JixinTxCodeEnum.PASSWORD_SET, passwordSetRequest);
        return ResponseEntity.ok(html);
    }

    @Override
    public ResponseEntity<VoHtmlResp> adminModifyOpenAccPwd(HttpServletRequest httpServletRequest, VoAdminModifyPasswordResp voAdminModifyPasswordResp) {
        if (!SecurityHelper.checkSign(voAdminModifyPasswordResp.getSign(), voAdminModifyPasswordResp.getParamStr())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "开户请求参数非法", VoHtmlResp.class));
        }

        // 开户主体信息, phone: 开户手机. name: 用户名称, userId: 用户ID, idNo:身份证号, cardNo: 银行卡号;
        Map<String, String> passwordMap = new Gson().fromJson(voAdminModifyPasswordResp.getParamStr(), TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long userId = Long.parseLong(passwordMap.get("userId"));

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前账户还未实名", VoHtmlResp.class));
        }

        String html = null;
        html = generateModifyPasswordHtml(httpServletRequest, userId, userThirdAccount);


        if (StringUtils.isEmpty(html)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }


        VoHtmlResp voHtmlResp = VoBaseResp.ok("成功", VoHtmlResp.class);
        try {
            voHtmlResp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl modifyOpenAccPwd gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }

        return ResponseEntity.ok(voHtmlResp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> delBank(HttpServletRequest httpServletRequest, Long userId) {
        // 查询本地账户余额/ 待收/ 待还
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        ResponseEntity<VoBaseResp> voBaseResp = ThirdAccountHelper.allConditionCheck(userThirdAccount);
        if (!voBaseResp.getStatusCode().equals(HttpStatus.OK)) {
            return voBaseResp;
        }

        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset);
        if ((asset.getUseMoney().longValue() != 0)
                || (asset.getNoUseMoney().longValue() != 0)
                || (asset.getPayment().longValue() != 0)
                || (asset.getCollection().longValue() != 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "不满足解绑条件: 1.账户余额必须等于零, 2.待还和待收都等于零"));
        }
        CardBindItem cardInfoByThird = null;
        try {
            cardInfoByThird = findCardInfoByThird(userThirdAccount.getAccountId());
        } catch (Exception e) {
            log.error("银行卡解绑异常", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络异常,请稍后重试"));
        }

        String txnDate = cardInfoByThird.getTxnDate();
        Date nowDate = new Date();
        // 查询债权关系
        CreditDetailsQueryRequest creditDetailsQueryRequest = new CreditDetailsQueryRequest();
        creditDetailsQueryRequest.setAccountId(userThirdAccount.getAccountId());
        creditDetailsQueryRequest.setStartDate(txnDate);
        creditDetailsQueryRequest.setEndDate(DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMD_NUM));
        creditDetailsQueryRequest.setState("1");
        creditDetailsQueryRequest.setPageNum("1");
        creditDetailsQueryRequest.setPageSize("10");
        CreditDetailsQueryResponse creditDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.CREDIT_DETAILS_QUERY,
                creditDetailsQueryRequest,
                CreditDetailsQueryResponse.class);

        if (ObjectUtils.isEmpty(creditDetailsQueryResponse) || !JixinResultContants.SUCCESS.equalsIgnoreCase(creditDetailsQueryResponse.getRetCode())) {
            String msg = ObjectUtils.isEmpty(creditDetailsQueryResponse) ? "当前网络异常, 请稍后尝试!" : creditDetailsQueryResponse.getRetMsg();
            log.error(String.format("债权明细查询: %s", msg));
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        String subPacks = creditDetailsQueryResponse.getSubPacks();
        if (!StringUtils.isEmpty(subPacks)) {
            List<CreditDetailsQueryItem> creditDetailsQueryItemList = new Gson().fromJson(subPacks, new TypeToken<List<CreditDetailsQueryItem>>() {
            }.getType());
            if (!CollectionUtils.isEmpty(creditDetailsQueryItemList)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "不满足解绑条件: 1.账户余额必须等于零, 2.待还和待收都等于零"));
            }
        }

        // 查询即信账户余额
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            String msg = ObjectUtils.isEmpty(balanceQueryResponse) ? "当前网络异常, 请稍后尝试!" : balanceQueryResponse.getRetMsg();
            log.error(String.format("资金同步: %s", msg));
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        double currBal = NumberHelper.toDouble(balanceQueryResponse.getCurrBal()) * 100.0;
        if (currBal != 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "不满足解绑条件: 1.账户余额必须等于零, 2.待还和待收都等于零"));
        }

        CardUnbindRequest cardUnbindRequest = new CardUnbindRequest();
        cardUnbindRequest.setAccountId(userThirdAccount.getAccountId());
        cardUnbindRequest.setCardNo(userThirdAccount.getCardNo());
        cardUnbindRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        cardUnbindRequest.setIdNo(userThirdAccount.getIdNo());
        cardUnbindRequest.setMobile(userThirdAccount.getMobile());
        cardUnbindRequest.setName(userThirdAccount.getName());

        CardUnbindResponse cardUnbindResponse = jixinManager.send(JixinTxCodeEnum.CARD_UNBIND, cardUnbindRequest, CardUnbindResponse.class);
        if (ObjectUtils.isEmpty(cardUnbindResponse) || !JixinResultContants.SUCCESS.equalsIgnoreCase(cardUnbindResponse.getRetCode())) {
            String msg = ObjectUtils.isEmpty(cardUnbindResponse) ? "当前网络异常, 请稍后尝试!" : cardUnbindResponse.getRetMsg();
            log.error(String.format("解绑异常: %s", msg));
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        userThirdAccount.setCardNo("");
        userThirdAccount.setBankName("");
        userThirdAccount.setBankLogo("");
        userThirdAccount.setCardNoBindState(1);
        userThirdAccount.setUpdateAt(nowDate);
        userThirdAccountService.save(userThirdAccount);

        return ResponseEntity.ok(VoBaseResp.ok("银行卡解绑成功!"));
    }

    @Override
    public ResponseEntity<VoHtmlResp> bindBank(HttpServletRequest httpServletRequest, Long userId, String bankNo) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
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

        /*if (userThirdAccount.getAutoTransferState() != 1) {  // 审核
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动债权转让协议！", VoHtmlResp.class));
        }


        if (userThirdAccount.getAutoTenderState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动投标协议！", VoHtmlResp.class));
        }*/

        // 先判断是否已经绑定
        CardBindItem cardInfoByThird = null;
        try {
            // 已经绑定
            cardInfoByThird = findCardInfoByThird(userThirdAccount.getAccountId());
            String cardNo = cardInfoByThird.getCardNo();
            String bankName = null;
            // 获取银行卡信息
            try {
                BankBinHelper.BankInfo bankInfo = bankBinHelper.find(cardNo);
                if (ObjectUtils.isEmpty(bankInfo)) {
                    log.error("银行卡绑定前置: 查无此银行卡号, 如有问题请联系平台客服!");
                    return ResponseEntity
                            .badRequest()
                            .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "系统异常, 请联系客户", VoHtmlResp.class));
                }

                if (!bankInfo.getCardType().equals("借记卡")) {
                    log.error("银行卡绑定前置: 银行卡类型必须为借记卡!");
                    return ResponseEntity
                            .badRequest()
                            .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "系统异常, 请联系客户", VoHtmlResp.class));
                }

                bankName = bankInfo.getBankName();
            } catch (Throwable e) {
                log.error("银行卡绑定前置: 开户查询银行卡异常");
            }

            // 6 判断银行卡
            DictValue dictValue = null;
            try {
                dictValue = bankLimitCache.get(bankName);
            } catch (Throwable e) {
                log.error("银行卡绑定: 查询平台支持银行异常", e);
            }
            if (ObjectUtils.isEmpty(dictValue)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "系统异常, 请联系客户", VoHtmlResp.class));
            }

            userThirdAccount.setCardNoBindState(1);
            userThirdAccount.setCardNo(cardNo);
            userThirdAccount.setBankLogo(dictValue.getValue03());
            userThirdAccount.setBankName(bankName);
            userThirdAccountService.save(userThirdAccount);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "你已经绑定银行卡, 如需要操作请进行解绑银行卡!", VoHtmlResp.class));
        } catch (Exception e) {
            log.info("绑定银行卡: 查询用户银行卡为空");
        }

        String bankName = null;
        // 获取银行卡信息
        try {
            BankBinHelper.BankInfo bankInfo = bankBinHelper.find(bankNo);
            if (ObjectUtils.isEmpty(bankInfo)) {
                log.error("银行卡绑定: 查无此银行卡号, 如有问题请联系平台客服!");
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "查无此银行卡号, 如有问题请联系平台客服!", VoHtmlResp.class));
            }

            if (!bankInfo.getCardType().equals("借记卡")) {
                log.error("银行卡绑定: 银行卡类型必须为借记卡!");
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "银行卡类型必须为借记卡!", VoHtmlResp.class));
            }

            bankName = bankInfo.getBankName();
        } catch (Throwable e) {
            log.error("银行卡绑定: 开户查询银行卡异常");
        }

        // 6 判断银行卡
        DictValue dictValue = null;
        try {
            dictValue = bankLimitCache.get(bankName);
        } catch (Throwable e) {
            log.error("银行卡绑定: 查询平台支持银行异常", e);
        }
        if (ObjectUtils.isEmpty(dictValue)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("当前平台不支持%s", bankName), VoHtmlResp.class));
        }
        // 生成html
        CardBindRequest cardBindRequest = new CardBindRequest();
        cardBindRequest.setAccountId(userThirdAccount.getAccountId());
        cardBindRequest.setCardNo(bankNo);
        cardBindRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        cardBindRequest.setIdNo(userThirdAccount.getIdNo());
        cardBindRequest.setMobile(userThirdAccount.getMobile());
        cardBindRequest.setName(userThirdAccount.getName());
        cardBindRequest.setAcqRes(String.valueOf(userId));
        cardBindRequest.setRetUrl(String.format("%s%s%s", javaDomain, "/pub/bindCard/show/", userId));
        cardBindRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/third/bank/bind/callback"));

        String html = jixinManager.getHtml(JixinTxCodeEnum.CARD_BIND, cardBindRequest);
        VoHtmlResp voHtmlResp = VoBaseResp.ok("操作成功", VoHtmlResp.class);
        try {
            voHtmlResp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了, 请稍后重试!", VoHtmlResp.class));
        }

        return ResponseEntity.ok(voHtmlResp);
    }

    @Override
    public ResponseEntity<String> bankBindCallback(HttpServletRequest httpServletRequest) {
        CardBindResponse cardBindResponse = jixinManager.callback(httpServletRequest, new TypeToken<CardBindResponse>() {
        });
        if (ObjectUtils.isEmpty(cardBindResponse) || !cardBindResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            log.error("银行卡绑定回调: 信息异常");
            return ResponseEntity.ok("success");
        }

        String accountid = cardBindResponse.getAccountId();
        CardBindItem cardInfoByThird = null;
        try {
            cardInfoByThird = findCardInfoByThird(accountid);
        } catch (Exception e) {
            log.error("银行卡回调", e);
            return ResponseEntity.ok("success");
        }

        String cardNo = cardInfoByThird.getCardNo();

        String bankName = null;
        // 获取银行卡信息
        try {
            BankBinHelper.BankInfo bankInfo = bankBinHelper.find(cardNo);
            if (ObjectUtils.isEmpty(bankInfo)) {
                log.error("银行卡绑定: 查无此银行卡号, 如有问题请联系平台客服!");
                return ResponseEntity.ok("success");
            }

            if (!bankInfo.getCardType().equals("借记卡")) {
                log.error("银行卡绑定: 银行卡类型必须为借记卡!");
                return ResponseEntity.ok("success");
            }

            bankName = bankInfo.getBankName();
        } catch (Throwable e) {
            log.error("银行卡绑定: 开户查询银行卡异常");
        }

        // 6 判断银行卡
        DictValue dictValue = null;
        try {
            dictValue = bankLimitCache.get(bankName);
        } catch (Throwable e) {
            log.error("银行卡绑定: 查询平台支持银行异常", e);
        }
        if (ObjectUtils.isEmpty(dictValue)) {
            return ResponseEntity.ok("success");
        }

        Long userId = Long.parseLong(cardBindResponse.getAcqRes());
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        userThirdAccount.setCardNoBindState(1);
        userThirdAccount.setCardNo(cardNo);
        userThirdAccount.setBankLogo(dictValue.getValue03());
        userThirdAccount.setBankName(bankName);
        userThirdAccountService.save(userThirdAccount);
        return ResponseEntity.ok("success");
    }

    @Override
    public String showBindCard(Long id, Model model) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(id);
        model.addAttribute("h5Domain", h5Domain);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return "bindCard/faile";
        }

        if (userThirdAccount.getCardNoBindState() == 1) {
            return "bindCard/success";
        } else {
            return "bindCard/faile";
        }
    }

    @Override
    public UserThirdAccount queryUserThirdInfo(Long id) throws Exception {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(id);
        if (!ObjectUtils.isEmpty(userThirdAccount)) {
            return userThirdAccount;
        }

        Users user = userService.findById(id);
        Preconditions.checkNotNull(user, "UserThirdBizImpl.queryUserThirdInfo: user is null");
        String phone = user.getPhone();
        if (StringUtils.isEmpty(phone)) {
            throw new Exception("当前用户手机号为空");
        }

        AccountQueryByMobileRequest accountQueryByMobileRequest = new AccountQueryByMobileRequest();
        accountQueryByMobileRequest.setMobile(phone);
        AccountQueryByMobileResponse accountQueryByMobileResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_QUERY_BY_MOBILE,
                accountQueryByMobileRequest,
                AccountQueryByMobileResponse.class);
        if (ObjectUtils.isEmpty(accountQueryByMobileResponse)
                || !JixinResultContants.SUCCESS.equals(accountQueryByMobileResponse.getRetCode())) {
            String msg = ObjectUtils.isEmpty(accountQueryByMobileResponse) ? "网路异常请稍后再试" : accountQueryByMobileResponse.getRetMsg();
            throw new Exception(msg);
        }

        String accountId = accountQueryByMobileResponse.getAccountId(); // 用户类型
        String mobile = accountQueryByMobileResponse.getMobile();// 手机号
        String idNo = accountQueryByMobileResponse.getIdNo(); // 证件号
        String name = accountQueryByMobileResponse.getName(); // 用户真实姓名

        UserThirdAccount entity = userThirdAccountService.findByDelUseid(user.getId());
        Date nowDate = new Date();
        if (ObjectUtils.isEmpty(entity)) {
            entity = new UserThirdAccount();
            entity.setCreateAt(nowDate);
            // 新用户只能主动查询用户银行卡
            try {
                CardBindItem cardInfoByThird = findCardInfoByThird(accountId); // 查询银行卡
                String cardNo = cardInfoByThird.getCardNo();
                BankBinHelper.BankInfo bankInfo = bankBinHelper.find(cardNo);
                if (ObjectUtils.isEmpty(bankInfo) || !bankInfo.getCardType().equals("借记卡")) {
                    throw new Exception("系统异常");
                }
                String bankName = bankInfo.getBankName();
                if (StringUtils.isEmpty(bankName)) {
                    throw new Exception("系统异常");
                }

                DictValue dictValue = bankLimitCache.get(bankName);
                entity.setBankLogo(dictValue.getValue03());
                entity.setBankName(bankName);
            } catch (Exception e) {
                log.error("系统主动查询开户信息并且写入银行卡信息异常");
            }
        }

        entity.setUserId(user.getId());
        entity.setUpdateAt(nowDate);
        entity.setUserId(user.getId());
        entity.setCreateId(user.getId());
        entity.setDel(0);
        entity.setMobile(mobile);
        entity.setIdType(1);
        entity.setIdNo(idNo);
        entity.setAcctUse(1);
        entity.setAccountId(accountId);
        entity.setPasswordState(0);
        entity.setCardNoBindState(1);
        entity.setName(name);
        userThirdAccountService.save(entity);


        //  9.保存用户实名信息
        user.setRealname(name);
        user.setCardId(idNo);
        user.setUpdatedAt(nowDate);
        userService.save(user);
        // 开户成功
        touchMarketingByOpenAccount(entity);
        return userThirdAccountService.findByUserId(user.getId());
    }


    /**
     * 根据开户账号查询绑定银行卡信息(通过查询存管平台)
     *
     * @param accountId
     * @return
     * @throws Exception
     */
    private CardBindItem findCardInfoByThird(String accountId) throws Exception {
        CardBindDetailsQueryRequest cardBindDetailsQueryRequest = new CardBindDetailsQueryRequest();
        cardBindDetailsQueryRequest.setAccountId(accountId);
        cardBindDetailsQueryRequest.setState("1");
        CardBindDetailsQueryResponse cardBindDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.CARD_BIND_DETAILS_QUERY,
                cardBindDetailsQueryRequest,
                CardBindDetailsQueryResponse.class);

        if (ObjectUtils.isEmpty(cardBindDetailsQueryResponse)
                || !cardBindDetailsQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            String msg = ObjectUtils.isEmpty(cardBindDetailsQueryResponse) ? " 查询银行卡信息, 网络请求超时"
                    : cardBindDetailsQueryResponse.getRetMsg();
            throw new Exception(msg);
        }

        Gson gson = new Gson();
        String subPacks = cardBindDetailsQueryResponse.getSubPacks();
        if (StringUtils.isEmpty(subPacks)) {
            throw new Exception(String.format("银行卡信息为空 %s", gson.toJson(cardBindDetailsQueryResponse)));
        }

        List<CardBindItem> cardBindItemsList = gson.fromJson(subPacks, new TypeToken<List<CardBindItem>>() {
        }.getType());
        if (CollectionUtils.isEmpty(cardBindItemsList)) {
            throw new Exception(String.format("银行卡信息为空 %s", gson.toJson(cardBindDetailsQueryResponse)));
        }

        return cardBindItemsList.get(0);
    }


    /**
     * 生成修改密码html
     *
     * @param httpServletRequest
     * @param userId
     * @param userThirdAccount
     * @return
     */
    private String generateModifyPasswordHtml(HttpServletRequest httpServletRequest, Long userId, UserThirdAccount userThirdAccount) {
        String html;// 判断用户是密码初始化还是
        boolean passwordState = openAccountBiz.findPasswordStateIsInitByUserId(userThirdAccount);
        // 请求即信获取密码期详情
        if (!passwordState) { // 初始化密码
            PasswordSetRequest passwordSetRequest = new PasswordSetRequest();
            passwordSetRequest.setMobile(userThirdAccount.getMobile());
            passwordSetRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
            passwordSetRequest.setName(userThirdAccount.getName());
            passwordSetRequest.setAccountId(userThirdAccount.getAccountId());
            passwordSetRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
            passwordSetRequest.setIdNo(userThirdAccount.getIdNo());
            passwordSetRequest.setAcqRes(String.valueOf(userId));
            //passwordSetRequest.setRetUrl(String.format("%s%s/%s", javaDomain, "/pub/password/show", userId));
            passwordSetRequest.setRetUrl(String.format("%s/pub/openAccount/callback/%s/initPassword", javaDomain, userThirdAccount.getUserId()));
            passwordSetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/1"));
            html = jixinManager.getHtml(JixinTxCodeEnum.PASSWORD_SET, passwordSetRequest);
        } else { // 重置密码
            PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
            passwordResetRequest.setMobile(userThirdAccount.getMobile());
            passwordResetRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
            passwordResetRequest.setName(userThirdAccount.getName());
            passwordResetRequest.setAccountId(userThirdAccount.getAccountId());
            passwordResetRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
            passwordResetRequest.setIdNo(userThirdAccount.getIdNo());
            passwordResetRequest.setAcqRes(String.valueOf(userId));
            passwordResetRequest.setRetUrl(String.format("%s%s/%s", javaDomain, "/pub/password/show", userId));
            passwordResetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/2"));
            html = jixinManager.getHtml(JixinTxCodeEnum.PASSWORD_RESET, passwordResetRequest);
        }
        return html;
    }

    /**
     * 查询用户面状态(会主动请求即信信息,同步数据库)
     *
     * @param userThirdAccount
     * @return
     */
    private Integer queryUserThirdPasswordState(UserThirdAccount userThirdAccount) {
        Integer passwordState = userThirdAccount.getPasswordState();
        if (passwordState.equals(0)) {
            // 查询密码
            PasswordSetQueryRequest passwordSetQueryRequest = new PasswordSetQueryRequest();
            passwordSetQueryRequest.setAccountId(userThirdAccount.getAccountId());
            PasswordSetQueryResponse passwordSetQueryResponse = jixinManager.send(JixinTxCodeEnum.PASSWORD_SET_QUERY,
                    passwordSetQueryRequest,
                    PasswordSetQueryResponse.class);
            if (ObjectUtils.isEmpty(passwordSetQueryResponse)
                    || JixinResultContants.SUCCESS.equals(passwordSetQueryResponse.getRetCode())) {
                return 0;
            }

            String pinFlag = passwordSetQueryResponse.getPinFlag();
            if ("1".equals(pinFlag)) { // 已经设置过密码, 同步数据库
                userThirdAccount.setPasswordState(1);
                userThirdAccountService.save(userThirdAccount);
            }
            return 1;


        } else {
            return 1;
        }
    }


    /**
     * 查询用户当天交易记录
     *
     * @param userAccountThirdTxReq
     * @return
     */
    @Override
    public ResponseEntity<UserAccountThirdTxRes> queryAccountTx(VoPcDoFirstVerity voPcDoFirstVerity) {

        String paramStr = voPcDoFirstVerity.getParamStr();
        //判断请求参数是否为空
        UserAccountThirdTxReq userAccountThirdTxReq = null;
        if (StringUtils.isEmpty(paramStr) || StringUtils.isEmpty(voPcDoFirstVerity.getSign())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc查询存管用户,请求参数字符串为空", UserAccountThirdTxRes.class));
        }
        //验证签名
        if (!SecurityHelper.checkSign(voPcDoFirstVerity.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc查询存管用户,签名验证不通过!", UserAccountThirdTxRes.class));
        }

        try {
            //json参数转对象
            userAccountThirdTxReq = new Gson().fromJson(voPcDoFirstVerity.getParamStr(),
                    new TypeToken<UserAccountThirdTxReq>() {
                    }.getType());
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc查询存管用户,请求参数字符串转对象失败", UserAccountThirdTxRes.class));
        }

        UserAccountThirdTxRes thridTxRes = VoBaseResp.ok("查询成功", UserAccountThirdTxRes.class);
        //查询交易时间
        String txDateStr = jixinTxDateHelper.getTxDateStr();

        //用户是否为空
        Users users = userService.findById(userAccountThirdTxReq.getUserId());
        if (ObjectUtils.isEmpty(users)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求", UserAccountThirdTxRes.class));
        }
        //用户是否开通存管
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(users.getId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户没有开通存管", UserAccountThirdTxRes.class));
        }
        // 存管账户ID
        String accountId = userThirdAccount.getAccountId();
        //分页
        Integer pageIndex = userAccountThirdTxReq.getPageIndex() + 1;
        Integer pageSize = userAccountThirdTxReq.getPageSize();
        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>();
        //装配请求即信请求参数
        AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
        accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex)); //启始页
        accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));   //页面大小
        // 查询交易时间范围
        accountDetailsQueryRequest.setStartDate(txDateStr);
        accountDetailsQueryRequest.setEndDate(txDateStr);
        //交易量；类型
        accountDetailsQueryRequest.setType(userAccountThirdTxReq.getType());
        //存管账户
        accountDetailsQueryRequest.setAccountId(accountId);
        //发送即信请求
        AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                accountDetailsQueryRequest,
                AccountDetailsQueryResponse.class);
        //判断返回结果
        if ((ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
            String ressultMsg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, ressultMsg, UserAccountThirdTxRes.class));
        }

        String subPacks = accountDetailsQueryResponse.getSubPacks();
        //判断交易流水是否为空
        if (StringUtils.isEmpty(subPacks) || subPacks.equals("[]")) {
            thridTxRes.setDetailsQueryItems(new ArrayList<>(0));
            return ResponseEntity.ok(thridTxRes);
        }
        //json转对象
        List<AccountDetailsQueryItem> detailsQueryItems = new Gson().fromJson(subPacks, new TypeToken<List<AccountDetailsQueryItem>>() {
        }.getType());
        thridTxRes.setTotalCount(Integer.valueOf(accountDetailsQueryResponse.getTotalItems()));
        thridTxRes.setDetailsQueryItems(detailsQueryItems);
        return ResponseEntity.ok(thridTxRes);
    }

    @Override
    public ResponseEntity<VoBaseResp> companyAccountInfo(String paramStr) {
        Gson gson = new Gson();
        Map<String, String> data = gson.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String id = data.get("id");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(id);
        Preconditions.checkNotNull(userThirdAccount, "当前账户为空");
        CorprationQueryRequest corprationQueryRequest = new CorprationQueryRequest();
        corprationQueryRequest.setAccountId(userThirdAccount.getAccountId());
        CorprationQueryResponse corprationQueryResponse = jixinManager.send(JixinTxCodeEnum.CORPRATION_QUERY, corprationQueryRequest, CorprationQueryResponse.class);
        log.info("================================");
        log.info("查询结果:" + gson.toJson(corprationQueryResponse));
        log.info("================================");


        CardBindDetailsQueryRequest cardBindDetailsQueryRequest = new CardBindDetailsQueryRequest();
        cardBindDetailsQueryRequest.setAccountId(userThirdAccount.getAccountId());
        cardBindDetailsQueryRequest.setState("0");
        CardBindDetailsQueryResponse cardBindDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.CARD_BIND_DETAILS_QUERY, cardBindDetailsQueryRequest, CardBindDetailsQueryResponse.class);
        log.info("================================");
        log.info("查询结果:" + gson.toJson(cardBindDetailsQueryResponse));
        log.info("================================");
        VoBaseResp voBaseResp = VoBaseResp.ok("查询成功");
        return ResponseEntity.ok(voBaseResp);
    }
}
