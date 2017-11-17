package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.auto_bid_auth_plus.AutoBidAuthRequest;
import com.gofobao.framework.api.model.auto_credit_invest_auth.AutoCreditInvestAuthRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.password_set.PasswordSetRequest;
import com.gofobao.framework.api.model.password_set_query.PasswordSetQueryRequest;
import com.gofobao.framework.api.model.password_set_query.PasswordSetQueryResponse;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.helper.ThirdAccountPasswordHelper;
import com.gofobao.framework.member.biz.OpenAccountBiz;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;

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
                /*String title = "广富宝开户结果页面";
                String errorMessage = "开户成功!";
                String buttonMessage = "返回资产中心";
                return generateCommon(title, errorMessage, buttonMessage, model, true);*/


                boolean autoTransferState = findAutoTransferStateByUserId(userThirdAccount);
                if (autoTransferState) {  // 开户成功
                    String title = "广富宝开户结果页面";
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

        AutoBidAuthRequest autoBidAuthRequest = new AutoBidAuthRequest();
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
        if (1 == userThirdAccount.getAutoTransferState()) {  // 审核
            return true;
        }

        // 进一步查询即信自动债权转让状态, 当平台与存管不一致, 会进行同步
        CreditAuthQueryRequest creditAuthQueryRequest = new CreditAuthQueryRequest();
        creditAuthQueryRequest.setAccountId(userThirdAccount.getAccountId());
        creditAuthQueryRequest.setType("2");
        creditAuthQueryRequest.setChannel(ChannelContant.APP);
        CreditAuthQueryResponse creditAuthQueryResponse = jixinManager
                .send(JixinTxCodeEnum.CREDIT_AUTH_QUERY, creditAuthQueryRequest, CreditAuthQueryResponse.class);
        if ((!ObjectUtils.isEmpty(creditAuthQueryResponse))
                && (creditAuthQueryResponse.getRetCode().equalsIgnoreCase(JixinResultContants.SUCCESS))) {
            if (creditAuthQueryResponse.getState().equalsIgnoreCase("1")) {
                userThirdAccount.setUpdateAt(new Date());
                userThirdAccount.setAutoTransferState(1);
                userThirdAccount.setAutoTransferBondOrderId(creditAuthQueryResponse.getOrderId());
                userThirdAccountService.save(userThirdAccount);
                return true;
            } else {
                return false;
            }
        } else {
            String msg = ObjectUtils.isArray(creditAuthQueryResponse) ?
                    "查询自动投标签约状态异常" : creditAuthQueryResponse.getRetMsg();
            log.error(String.format("查询即信自动投标状态异: %s", msg));
            return false;
        }

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
                continue;
            }

            if (JixinResultContants.SUCCESS.equalsIgnoreCase(creditAuthQueryResponse.getRetCode())
                    && ("1".equalsIgnoreCase(creditAuthQueryResponse.getState()))) {
                userThirdAccount.setUpdateAt(new Date());
                userThirdAccount.setAutoTenderState(1);
                userThirdAccount.setAutoTenderOrderId(creditAuthQueryResponse.getOrderId());
                userThirdAccount.setAutoTenderTotAmount(999999999L);
                userThirdAccount.setAutoTenderTxAmount(999999999L);
                userThirdAccountService.save(userThirdAccount);
                return true;
            } else {
                return false;
            }
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
        // 对于未同步, 需要查询即信进行进一步确定密码设置情况; 如果遇到不一致,进行本地同步
        PasswordSetQueryRequest passwordSetQueryRequest = new PasswordSetQueryRequest();
        passwordSetQueryRequest.setAccountId(userThirdAccount.getAccountId());
        PasswordSetQueryResponse passwordSetQueryResponse = jixinManager.send(JixinTxCodeEnum.PASSWORD_SET_QUERY,
                passwordSetQueryRequest,
                PasswordSetQueryResponse.class);
        if (ObjectUtils.isEmpty(passwordSetQueryResponse)
                || !JixinResultContants.SUCCESS.equals(passwordSetQueryResponse.getRetCode())) {
            String msg = ObjectUtils.isEmpty(passwordSetQueryResponse) ?
                    "请求即信通讯异常" : passwordSetQueryResponse.getRetMsg();
            log.error(String.format("OpenAccountBizImpl.findPasswordStateInitByUserId: %s", msg));
            return false;
        }

        String pinFlag = passwordSetQueryResponse.getPinFlag();
        if ("1".equals(pinFlag)) { // 已经设置过密码, 同步数据库
            userThirdAccount.setPasswordState(1);
            userThirdAccountService.save(userThirdAccount);
            return true;
        }

        return false;
    }
}
