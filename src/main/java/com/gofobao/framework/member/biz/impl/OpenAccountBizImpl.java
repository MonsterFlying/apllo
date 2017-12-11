package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.contants.SrvTxCodeContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.auto_bid_auth_plus.AutoBidAuthPlusRequest;
import com.gofobao.framework.api.model.auto_credit_invest_auth.AutoCreditInvestAuthRequest;
import com.gofobao.framework.api.model.auto_credit_invest_auth_plus.AutoCreditInvestAuthPlusRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.password_reset.PasswordResetRequest;
import com.gofobao.framework.api.model.password_set.PasswordSetRequest;
import com.gofobao.framework.api.model.password_set_query.PasswordSetQueryRequest;
import com.gofobao.framework.api.model.password_set_query.PasswordSetQueryResponse;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.helper.ThirdAccountPasswordHelper;
import com.gofobao.framework.member.biz.OpenAccountBiz;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoAccountStatusResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class OpenAccountBizImpl implements OpenAccountBiz {

    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    JixinManager jixinManager;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${gofobao.pcDomain}")
    private String pcDomain;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    ThirdAccountPasswordHelper thirdAccountPasswordHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String opeanAccountCallBack(Long userId,
                                       String process,
                                       HttpServletRequest httpServletRequest,
                                       HttpServletResponse httpServletResponse,
                                       Model model) {
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "OpenAccountBizImpl.openAccountCallback: user is null");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "OpenAccountBizImpl.opeanAccountCallBack: userThirdAccount is null");
        // 判断用户密码是否设置
        boolean passwordState = findPasswordStateIsInitByUserId(userThirdAccount);
        if (passwordState) {
            // 查询自动投标签约状况
            boolean autoTenderState = findAutoTenderStateByUserId(userThirdAccount);
            if (autoTenderState) {
                /*if(!"financer".equalsIgnoreCase(users.getType())){
                    // 金服用户不需要债权转让
                    String title = "开户结果页面";
                    String errorMessage = "开户成功!";
                    String buttonMessage = "返回资产中心";
                    return generateCommon(title, errorMessage, buttonMessage, model, true);
                }else{
                    // 是理财用户必须签约
                    boolean autoTransferState = findAutoTransferStateByUserId(userThirdAccount);
                    if (autoTransferState) {  // 开户成功
                        String title = "开户结果页面";
                        String errorMessage = "开户成功!";
                        String buttonMessage = "返回资产中心";
                        return generateCommon(title, errorMessage, buttonMessage, model, true);
                    } else {
                        if ("autoTender".equals(process)) { // 自动投标成功千万自动债权转让
                            String title = "自动债权转让签约";
                            String errorMessage = "自动投标签约成功!";
                            String buttonMessage = "前往自动债权转让签约";
                            return generateAutoTransfer(userThirdAccount, httpServletRequest, model,
                                    title, errorMessage, buttonMessage, true);
                        } else { // 重新发起自动债权转让签约
                            String title = "自动债权转让签约";
                            String errorMessage = "自动债权转让签约失败, 重新签约!";
                            String buttonMessage = "前往自动债权转让签约";
                            return generateAutoTransfer(userThirdAccount, httpServletRequest, model,
                                    title, errorMessage, buttonMessage, false);
                        }
                    }
                }*/

                // 是理财用户必须签约
                boolean autoTransferState = findAutoTransferStateByUserId(userThirdAccount);
                if (autoTransferState) {  // 开户成功
                    String title = "开户结果页面";
                    String errorMessage = "开户成功!";
                    String buttonMessage = "返回资产中心";
                    return generateCommon(title, errorMessage, buttonMessage, model, true);
                } else {
                    if ("autoTender".equals(process)) { // 自动投标成功千万自动债权转让
                        String title = "自动债权转让签约";
                        String errorMessage = "自动投标签约成功!";
                        String buttonMessage = "前往自动债权转让签约";
                        return generateAutoTransfer(userThirdAccount, httpServletRequest, model,
                                title, errorMessage, buttonMessage, true);
                    } else { // 重新发起自动债权转让签约
                        String title = "自动债权转让签约";
                        String errorMessage = "自动债权转让签约失败, 重新签约!";
                        String buttonMessage = "前往自动债权转让签约";
                        return generateAutoTransfer(userThirdAccount, httpServletRequest, model,
                                title, errorMessage, buttonMessage, false);
                    }
                }
            } else {
                if ("initPassword".equals(process)) { // 设置密码成功, 前往自动投标签约
                    String title = "自动投标签约";
                    String errorMessage = "存管账户密码设置成功";
                    String buttonMessage = "前往自动投标签约";
                    return generateAutoTender(userThirdAccount, httpServletRequest, model, title,
                            errorMessage, buttonMessage, true);
                } else {    // 重新分装自动投标签约页面
                    String title = "自动投标签约";
                    String errorMessage = "自动投标签约失败, 请重新签约!";
                    String buttonMessage = "前往自动投标签约";
                    return generateAutoTender(userThirdAccount, httpServletRequest, model, title,
                            errorMessage, buttonMessage, false);
                }
            }
        } else {
            if ("initPassword".equals(process)) {  // 重新设置密码
                // 获取设置密码的html
                String title = "初始化密码";
                String errorMessage = "设置存管账户密码失败, 请重新设置密码!";
                String buttonMessage = "重新设置密码";
                return generateInitPassword(userThirdAccount,
                        httpServletRequest,
                        model,
                        title,
                        errorMessage,
                        buttonMessage);  // 错误提示, 并且重新尝试设置密码
            } else {  // 出现异常
                String title = "存管开户";
                String errorMessage = "开户出现未知异常, 请联系平台客服!";
                String buttonMessage = "返回";
                return generateCommon(title, errorMessage, buttonMessage, model, false);
            }
        }
    }

    /**
     * 生成通用页面
     *
     * @param title
     * @param errorMessage
     * @param buttonMessage
     * @param model
     * @param isSuccess     @return
     */
    private String generateCommon(String title, String errorMessage, String buttonMessage, Model model, boolean isSuccess) {
        String url = String.format("%s/#/user", h5Domain);
        model.addAttribute("title", title);
        model.addAttribute("message", errorMessage);
        model.addAttribute("action", url);
        model.addAttribute("buttonMessage", buttonMessage);
        return isSuccess ? "openAccount/commonSuccess" : "openAccount/commonFail";
    }


    /**
     * 自动债权转让
     *
     * @param userThirdAccount
     * @param httpServletRequest
     * @param model
     * @param title
     * @param errorMessage
     * @param buttonMessage
     * @param isSuccess
     * @return0
     */
    private String generateAutoTransfer(UserThirdAccount userThirdAccount,
                                        HttpServletRequest httpServletRequest,
                                        Model model,
                                        String title,
                                        String errorMessage,
                                        String buttonMessage,
                                        boolean isSuccess) {

        AutoCreditInvestAuthRequest autoCreditInvestAuthPlusRequest = new AutoCreditInvestAuthRequest();
        autoCreditInvestAuthPlusRequest.setAccountId(userThirdAccount.getAccountId());
        autoCreditInvestAuthPlusRequest.setOrderId(System.currentTimeMillis() + RandomHelper.generateNumberCode(6));
        autoCreditInvestAuthPlusRequest.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userThirdAccount.getUserId()));
        autoCreditInvestAuthPlusRequest.setRetUrl(String.format("%s/pub/openAccount/callback/%s/autoTranfer", javaDomain, userThirdAccount.getUserId()));
        autoCreditInvestAuthPlusRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/user/third/autoTranfer/callback"));
        autoCreditInvestAuthPlusRequest.setAcqRes(userThirdAccount.getUserId().toString());
        autoCreditInvestAuthPlusRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
        String url = jixinManager.getUrl(JixinTxCodeEnum.AUTO_CREDIT_INVEST_AUTH);
        List<JixinManager.KeyValuePair> datas = jixinManager.getSignData(JixinTxCodeEnum.AUTO_CREDIT_INVEST_AUTH, autoCreditInvestAuthPlusRequest);
        model.addAttribute("title", title);
        model.addAttribute("datas", datas);
        model.addAttribute("message", errorMessage);
        model.addAttribute("action", url);
        model.addAttribute("buttonMessage", buttonMessage);
        return isSuccess ? "openAccount/formSuccess" : "openAccount/formFail";
    }

    /**
     * 生成自动投标
     *
     * @param userThirdAccount
     * @param httpServletRequest
     * @param model
     * @param title
     * @param errorMessage
     * @param buttonMessage
     * @param isSuccess
     * @return
     */
    private String generateAutoTender(UserThirdAccount userThirdAccount,
                                      HttpServletRequest httpServletRequest,
                                      Model model,
                                      String title,
                                      String errorMessage,
                                      String buttonMessage,
                                      boolean isSuccess) {

        AutoBidAuthPlusRequest autoBidAuthRequest = new AutoBidAuthPlusRequest();
        autoBidAuthRequest.setAccountId(userThirdAccount.getAccountId());
        autoBidAuthRequest.setOrderId(System.currentTimeMillis() + RandomHelper.generateNumberCode(6));
        autoBidAuthRequest.setTxAmount("999999999");
        autoBidAuthRequest.setTotAmount("999999999");
        autoBidAuthRequest.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userThirdAccount.getUserId()));
        autoBidAuthRequest.setRetUrl(String.format("%s/pub/openAccount/callback/%s/autoTender", javaDomain, userThirdAccount.getUserId()));
        autoBidAuthRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/user/third/autoTender/callback"));
        autoBidAuthRequest.setAcqRes(userThirdAccount.getUserId().toString());
        autoBidAuthRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
        String url = jixinManager.getUrl(JixinTxCodeEnum.AUTO_BID_AUTH);
        List<JixinManager.KeyValuePair> datas = jixinManager.getSignData(JixinTxCodeEnum.AUTO_BID_AUTH, autoBidAuthRequest);
        model.addAttribute("title", title);
        model.addAttribute("datas", datas);
        model.addAttribute("message", errorMessage);
        model.addAttribute("action", url);
        model.addAttribute("buttonMessage", buttonMessage);
        return isSuccess ? "openAccount/formSuccess" : "openAccount/formFail";
    }


    /**
     * 生成存管账户初始化密码html
     *
     * @param userThirdAccount
     * @param httpServletRequest
     * @param model
     * @param title
     * @param errorMessage
     * @param buttonMessage
     * @return
     */
    private String generateInitPassword(UserThirdAccount userThirdAccount,
                                        HttpServletRequest httpServletRequest,
                                        Model model,
                                        String title,
                                        String errorMessage,
                                        String buttonMessage) {
        String url = jixinManager.getUrl(JixinTxCodeEnum.PASSWORD_SET);
        PasswordSetRequest passwordSetRequest = new PasswordSetRequest();
        passwordSetRequest.setMobile(userThirdAccount.getMobile());
        passwordSetRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
        passwordSetRequest.setName(userThirdAccount.getName());
        passwordSetRequest.setAccountId(userThirdAccount.getAccountId());
        passwordSetRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        passwordSetRequest.setIdNo(userThirdAccount.getIdNo());
        passwordSetRequest.setAcqRes(String.valueOf(userThirdAccount.getUserId()));
        passwordSetRequest.setRetUrl(String.format("%s/pub/openAccount/callback/%s/initPassword", javaDomain, userThirdAccount.getUserId()));
        passwordSetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/1"));
        List<JixinManager.KeyValuePair> datas = jixinManager.getSignData(JixinTxCodeEnum.PASSWORD_SET, passwordSetRequest);
        model.addAttribute("title", title);
        model.addAttribute("datas", datas);
        model.addAttribute("message", errorMessage);
        model.addAttribute("action", url);
        model.addAttribute("buttonMessage", buttonMessage);
        return "openAccount/formSuccess";
    }


    /**
     * 查询自动债权转让状态
     * 注意:
     * 如果发现本地与存管系统不一致, 会进行同步
     *
     * @param userThirdAccount 开户信息
     * @return true: 已经设置: false: 未设置
     */
    private boolean findAutoTransferStateByUserId(UserThirdAccount userThirdAccount) {
        if (userThirdAccount.getAutoTransferState().equals(1)) {  // 审核
            log.info("[查询自动债权转让] 已经签署");
            return true;
        }
        CreditAuthQueryRequest creditAuthQueryRequest = new CreditAuthQueryRequest();
        int looper = 5;
        do {
            creditAuthQueryRequest.setTxTime(null);
            creditAuthQueryRequest.setTxDate(null);
            creditAuthQueryRequest.setSeqNo(null);
            creditAuthQueryRequest.setAccountId(userThirdAccount.getAccountId());
            creditAuthQueryRequest.setType("2");
            creditAuthQueryRequest.setChannel(ChannelContant.APP);
            CreditAuthQueryResponse creditAuthQueryResponse = jixinManager
                    .send(JixinTxCodeEnum.CREDIT_AUTH_QUERY, creditAuthQueryRequest, CreditAuthQueryResponse.class);

            if (JixinResultContants.isNetWordError(creditAuthQueryResponse)
                    || JixinResultContants.isBusy(creditAuthQueryResponse)) {
                log.error("[查询自动债权转让] 网络异常");
                continue;
            }

            if (JixinResultContants.SUCCESS.equalsIgnoreCase(creditAuthQueryResponse.getRetCode())
                    && creditAuthQueryResponse.getState().equalsIgnoreCase("1")) {
                log.info("[查询自动债权转让] 执行同步");
                userThirdAccount.setUpdateAt(new Date());
                userThirdAccount.setAutoTransferState(1);
                userThirdAccount.setAutoTransferBondOrderId(creditAuthQueryResponse.getOrderId());
                userThirdAccountService.save(userThirdAccount);
                return true;
            }
            log.warn("[查询自动债权转让] failure");
            break;
        } while (looper > 0);
        return false;
    }

    /**
     * 查询自动投标状态
     * 注意:
     * 如果发现本地与存管系统不一致, 会进行同步
     *
     * @param userThirdAccount 开户信息
     * @return true: 已经设置: false: 未设置
     */
    private boolean findAutoTenderStateByUserId(UserThirdAccount userThirdAccount) {
        if (1 == userThirdAccount.getAutoTenderState()) {
            return true;
        }
        CreditAuthQueryRequest creditAuthQueryRequest = new CreditAuthQueryRequest();
        int looper = 5;
        do {
            looper--;
            creditAuthQueryRequest.setTxDate(null);
            creditAuthQueryRequest.setTxTime(null);
            creditAuthQueryRequest.setSeqNo(null);
            creditAuthQueryRequest.setAccountId(userThirdAccount.getAccountId());
            creditAuthQueryRequest.setType("1");
            creditAuthQueryRequest.setChannel(ChannelContant.APP);
            CreditAuthQueryResponse creditAuthQueryResponse = jixinManager
                    .send(JixinTxCodeEnum.CREDIT_AUTH_QUERY, creditAuthQueryRequest, CreditAuthQueryResponse.class);
            if (JixinResultContants.isNetWordError(creditAuthQueryResponse)
                    || JixinResultContants.isBusy(creditAuthQueryResponse)) {
                log.warn("[自动投标协议查询] 网络异常, 重试");
                continue;
            }

            if (JixinResultContants.SUCCESS.equalsIgnoreCase(creditAuthQueryResponse.getRetCode())
                    && ("1".equalsIgnoreCase(creditAuthQueryResponse.getState()))) {
                log.warn("[自动投标协议查询] 同步自动投标协议");
                userThirdAccount.setUpdateAt(new Date());
                userThirdAccount.setAutoTenderState(1);
                userThirdAccount.setAutoTenderOrderId(creditAuthQueryResponse.getOrderId());
                userThirdAccount.setAutoTenderTotAmount(999999999L);
                userThirdAccount.setAutoTenderTxAmount(999999999L);
                userThirdAccountService.save(userThirdAccount);
                return true;
            }
            log.warn("[自动投标协议查询] failure");
            break;
        } while (looper > 0);
        return false;
    }


    /**
     * 查找存管密码是否设置
     * 注意:
     * 如果发现本地与存管系统不一致,会进行同步
     *
     * @param userThirdAccount 开户信息
     * @return true: 已经初始化, false: 未初始化
     */
    public boolean findPasswordStateIsInitByUserId(UserThirdAccount userThirdAccount) {
        if (userThirdAccount.getPasswordState() == 1) {
            return true;
        }
        PasswordSetQueryRequest passwordSetQueryRequest = new PasswordSetQueryRequest();
        int looper = 5;
        Date nowDate = new Date();
        do {
            --looper;
            passwordSetQueryRequest.setTxDate(null);
            passwordSetQueryRequest.setTxTime(null);
            passwordSetQueryRequest.setSeqNo(null);
            passwordSetQueryRequest.setAccountId(userThirdAccount.getAccountId());
            PasswordSetQueryResponse passwordSetQueryResponse = jixinManager.send(JixinTxCodeEnum.PASSWORD_SET_QUERY,
                    passwordSetQueryRequest,
                    PasswordSetQueryResponse.class);
            if (JixinResultContants.isBusy(passwordSetQueryResponse)
                    || JixinResultContants.isNetWordError(passwordSetQueryResponse)) {
                log.error(String.format("[查询开户密码]: 尝试再次查询 %s", passwordSetQueryResponse.getRetMsg()));
                continue;
            }

            if (JixinResultContants.SUCCESS.equalsIgnoreCase(passwordSetQueryResponse.getRetCode())) {
                String pinFlag = passwordSetQueryResponse.getPinFlag();
                if ("1".equals(pinFlag)) { // 已经设置过密码, 同步数据库
                    log.info("[查询开户密码] 主动进行密码设置同步");
                    userThirdAccount.setPasswordState(1);
                    userThirdAccount.setUpdateAt(nowDate);
                    userThirdAccountService.save(userThirdAccount);
                    return true;
                }
            }
            log.warn(String.format("[查询开户密码] %s", passwordSetQueryResponse.getRetMsg()));
            break;
        } while (looper > 0);
        return false;
    }

    @Override
    public ResponseEntity<VoHtmlResp> accountPasswordManagement(@NonNull HttpServletRequest httpServletRequest,
                                                                @NonNull Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "前先开通银行存管", VoHtmlResp.class));
        }
        String html;
        try {
            html = doGenaratePasswordManagementHtml(httpServletRequest, userThirdAccount);
        } catch (Exception e) {
            log.error("[存管交易密码管理]", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不稳定, 请稍后再次尝试!", VoHtmlResp.class));
        }

        VoHtmlResp result = VoBaseResp.ok("成功", VoHtmlResp.class);
        result.setHtml(html);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<VoHtmlResp> acocuntAuthorizeTender(@NonNull HttpServletRequest httpServletRequest,
                                                             @NonNull String msgCode,
                                                             @NonNull Long userId) {
        UserThirdAccount account = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(account)) {
            log.warn("[平台投标授权] 用户未开户, 数据{}", userId);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "请前往存管开户页面", VoHtmlResp.class));
        }

        // 匹配本地存管密码设置标识, 如果未设置, 进一步使用网络重新查询存管系统是否设置.
        Integer passwordState = account.getPasswordState();
        if (passwordState.equals(0)) {
            log.warn("[平台投标授权] 未初始化交易密码, 数据{}", account.toString());
            boolean thirdPasswordSet = findPasswordStateIsInitByUserId(account);
            if (!thirdPasswordSet) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请前往初始化交易密码页面", VoHtmlResp.class));
            }
        }

        boolean tenderState = findAutoTenderStateByUserId(account);
        if (tenderState) {
            log.warn("[平台投标授权] 当前用户已经签署投标授权协议, 数据{}", account.toString());
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你已经签署该协议, 无需重复签署!", VoHtmlResp.class));
        }

        // 获取短信验证码
        String srvTxCode = null;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.AUTO_BID_AUTH_PLUS, account.getMobile()), null);
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.AUTO_BID_AUTH_PLUS, account.getMobile()));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl opeanAccountCallBack get redis exception ", e);
        }

        if (StringUtils.isEmpty(srvTxCode)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取", VoHtmlResp.class));
        }

        String channel = ChannelContant.getchannel(httpServletRequest);
        // 封装协议
        AutoBidAuthPlusRequest autoBidAuthPlusRequest = new AutoBidAuthPlusRequest();
        autoBidAuthPlusRequest.setAccountId(account.getAccountId());
        autoBidAuthPlusRequest.setOrderId(System.currentTimeMillis() + RandomHelper.generateNumberCode(6));
        autoBidAuthPlusRequest.setTxAmount("999999999");
        autoBidAuthPlusRequest.setTotAmount("999999999");
        autoBidAuthPlusRequest.setLastSrvAuthCode(srvTxCode);
        autoBidAuthPlusRequest.setSmsCode(msgCode);
        autoBidAuthPlusRequest.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userId));
        if (ChannelContant.APP.equalsIgnoreCase(channel)) {
            // TODO 平台投标授权  app链接
            autoBidAuthPlusRequest.setRetUrl("javascript:closeTender();");
        } else if (ChannelContant.HTML.equalsIgnoreCase(channel)) {
            // TODO 平台投标授权  pc链接
            autoBidAuthPlusRequest.setRetUrl(String.format("%s/", javaDomain, ""));
        } else if (ChannelContant.WE_CHAT.equalsIgnoreCase(channel)) {
            // TODO 平台投标授权  H5链接
            autoBidAuthPlusRequest.setRetUrl(String.format("%s/ r", javaDomain, ""));
        } else {
            log.warn("[平台投标授权] requestSource 为空, 数据{}", account.toString());
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网路异常,请稍后再次尝试!", VoHtmlResp.class));
        }
        autoBidAuthPlusRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/user/third/autoTender/callback"));
        autoBidAuthPlusRequest.setAcqRes(userId.toString());
        autoBidAuthPlusRequest.setChannel(channel);
        String html = jixinManager.getHtml(JixinTxCodeEnum.AUTO_BID_AUTH_PLUS, autoBidAuthPlusRequest);
        VoHtmlResp result = VoBaseResp.ok("成功", VoHtmlResp.class);
        result.setHtml(html);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<VoHtmlResp> acocuntAuthorizeTransfer(HttpServletRequest httpServletRequest, String msgCode, Long userId) {
        UserThirdAccount account = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(account)) {
            log.warn("[债权转让授权] 用户未开户, 数据{}", userId);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "请前往存管开户页面", VoHtmlResp.class));
        }

        // 匹配本地存管密码设置标识, 如果未设置, 进一步使用网络重新查询存管系统是否设置.
        Integer passwordState = account.getPasswordState();
        if (passwordState.equals(0)) {
            log.warn("[债权转让授权] 未初始化交易密码, 数据{}", account.toString());
            boolean thirdPasswordSet = findPasswordStateIsInitByUserId(account);
            if (!thirdPasswordSet) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请前往初始化交易密码页面", VoHtmlResp.class));
            }
        }

        boolean transferState = findAutoTransferStateByUserId(account);
        if (transferState) {
            log.warn("[债权转让授权] 当前用户已经签署债权转让授权协议, 数据{}", account.toString());
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你已经签署该协议, 无需重复签署!", VoHtmlResp.class));
        }

        // 获取短信验证码
        String srvTxCode = null;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.AUTO_CREDIT_INVEST_AUTH_PLUS, account.getMobile()), null);
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.AUTO_CREDIT_INVEST_AUTH_PLUS, account.getMobile()));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl opeanAccountCallBack get redis exception ", e);
        }

        if (StringUtils.isEmpty(srvTxCode)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取", VoHtmlResp.class));
        }

        String channel = ChannelContant.getchannel(httpServletRequest);
        // 封装协议
        AutoCreditInvestAuthPlusRequest autoCreditInvestAuthPlusRequest = new AutoCreditInvestAuthPlusRequest();
        autoCreditInvestAuthPlusRequest.setAccountId(account.getAccountId());
        autoCreditInvestAuthPlusRequest.setOrderId(System.currentTimeMillis() + RandomHelper.generateNumberCode(6));
        autoCreditInvestAuthPlusRequest.setLastSrvAuthCode(srvTxCode);
        autoCreditInvestAuthPlusRequest.setSmsCode(msgCode);
        autoCreditInvestAuthPlusRequest.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userId));
        if (ChannelContant.APP.equalsIgnoreCase(channel)) {
            // TODO 平台投标授权  app链接
            autoCreditInvestAuthPlusRequest.setRetUrl("javascript:closeTender();");
        } else if (ChannelContant.HTML.equalsIgnoreCase(channel)) {
            // TODO 平台投标授权  pc链接
            autoCreditInvestAuthPlusRequest.setRetUrl(String.format("%s/", javaDomain, ""));
        } else if (ChannelContant.WE_CHAT.equalsIgnoreCase(channel)) {
            // TODO 平台投标授权  H5链接
            autoCreditInvestAuthPlusRequest.setRetUrl(String.format("%s/ r", javaDomain, ""));
        } else {
            log.warn("[债权转让授权] requestSource 为空, 数据{}", account.toString());
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网路异常,请稍后再次尝试!", VoHtmlResp.class));
        }

        autoCreditInvestAuthPlusRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/user/third/autoTranfer/callback"));
        autoCreditInvestAuthPlusRequest.setAcqRes(userId.toString());
        autoCreditInvestAuthPlusRequest.setChannel(channel);
        String html = jixinManager.getHtml(JixinTxCodeEnum.AUTO_CREDIT_INVEST_AUTH_PLUS, autoCreditInvestAuthPlusRequest);
        VoHtmlResp result = VoBaseResp.ok("成功", VoHtmlResp.class);
        result.setHtml(html);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<VoAccountStatusResp> acocuntConfigState(HttpServletRequest httpServletRequest, Long userId) {
        UserThirdAccount account = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(account)) {
            log.warn("[债权转让授权] 用户未开户, 数据{}", userId);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "请前往存管开户页面", VoAccountStatusResp.class));
        }

        boolean tenderState = findAutoTenderStateByUserId(account);
        boolean transferState = findAutoTransferStateByUserId(account);
        boolean passwordState = findPasswordStateIsInitByUserId(account);

        VoAccountStatusResp voAccountStatusResp = VoBaseResp.ok("成功", VoAccountStatusResp.class);
        voAccountStatusResp.setPasswordState(passwordState);
        voAccountStatusResp.setTransferState(transferState);
        voAccountStatusResp.setTenderState(tenderState);
        return ResponseEntity.ok(voAccountStatusResp);
    }

    /**
     * 生成
     *
     * @param httpServletRequest
     * @param userThirdAccount
     * @return
     */
    private String doGenaratePasswordManagementHtml(@NonNull HttpServletRequest httpServletRequest,
                                                    @NonNull UserThirdAccount userThirdAccount) throws Exception {
        boolean passwordState = findPasswordStateIsInitByUserId(userThirdAccount);
        String channel = ChannelContant.getchannel(httpServletRequest);
        if (passwordState) {
            // 重置密码
            PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
            passwordResetRequest.setMobile(userThirdAccount.getMobile());
            passwordResetRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
            passwordResetRequest.setName(userThirdAccount.getName());
            passwordResetRequest.setAccountId(userThirdAccount.getAccountId());
            passwordResetRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
            passwordResetRequest.setIdNo(userThirdAccount.getIdNo());
            passwordResetRequest.setAcqRes(String.valueOf(userThirdAccount.getUserId()));
            // 设置前端跳转前缀
            if (ChannelContant.APP.equalsIgnoreCase(channel)) {
                passwordResetRequest.setRetUrl("javascript:closePasswordReSetWindow();");
            } else if (ChannelContant.HTML.equalsIgnoreCase(channel)) {
                passwordResetRequest.setRetUrl(String.format("%s/%s", pcDomain, ""));
            } else if (ChannelContant.WE_CHAT.equalsIgnoreCase(channel)) {
                passwordResetRequest.setRetUrl(String.format("%s/%s", h5Domain, ""));
            } else {
                throw new Exception("[重置存管交易密码] requestSource 未知");
            }
            passwordResetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/2"));
            return jixinManager.getHtml(JixinTxCodeEnum.PASSWORD_RESET, passwordResetRequest);
        } else {
            // 初始化密码
            PasswordSetRequest passwordSetRequest = new PasswordSetRequest();
            passwordSetRequest.setMobile(userThirdAccount.getMobile());
            passwordSetRequest.setChannel(channel);
            passwordSetRequest.setName(userThirdAccount.getName());
            passwordSetRequest.setAccountId(userThirdAccount.getAccountId());
            passwordSetRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
            passwordSetRequest.setIdNo(userThirdAccount.getIdNo());
            passwordSetRequest.setAcqRes(String.valueOf(userThirdAccount.getUserId()));
            // 设置前端跳转前缀
            if (ChannelContant.APP.equalsIgnoreCase(channel)) {
                passwordSetRequest.setRetUrl("javascript:closePasswordInitWindow();");
            } else if (ChannelContant.HTML.equalsIgnoreCase(channel)) {
                passwordSetRequest.setRetUrl(String.format("%s/%s", pcDomain, ""));
            } else if (ChannelContant.WE_CHAT.equalsIgnoreCase(channel)) {
                passwordSetRequest.setRetUrl(String.format("%s/%s", h5Domain, ""));
            } else {
                throw new Exception("[初始化存管交易密码] requestSource 未知");
            }
            // 回调接口
            passwordSetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/1"));
            return jixinManager.getHtml(JixinTxCodeEnum.PASSWORD_SET, passwordSetRequest);
        }
    }
}
