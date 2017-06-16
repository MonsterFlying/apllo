package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.api.contants.*;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_open_plus.AccountOpenPlusRequest;
import com.gofobao.framework.api.model.account_open_plus.AccountOpenPlusResponse;
import com.gofobao.framework.api.model.auto_bid_auth_plus.auto_credit_invest_auth_plus.AutoCreditInvestAuthPlusRequest;
import com.gofobao.framework.api.model.auto_bid_auth_plus.auto_credit_invest_auth_plus.AutoCreditInvestAuthPlusResponse;
import com.gofobao.framework.api.model.auto_credit_invest_auth_plus.AutoBidAuthPlusRequest;
import com.gofobao.framework.api.model.auto_credit_invest_auth_plus.AutoBidAuthPlusResponse;
import com.gofobao.framework.api.model.password_reset.PasswordResetRequest;
import com.gofobao.framework.api.model.password_reset.PasswordResetResponse;
import com.gofobao.framework.api.model.password_set.PasswordSetRequest;
import com.gofobao.framework.api.model.password_set.PasswordSetResponse;
import com.gofobao.framework.asset.entity.BankAccount;
import com.gofobao.framework.asset.service.BankAccountService;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.request.VoOpenAccountReq;
import com.gofobao.framework.member.vo.response.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by Max on 17/5/22.
 */
@Service
@Slf4j
public class UserThirdBizImpl implements UserThirdBiz {

    @Autowired
    UserService userService ;

    @Autowired
    BankAccountService bankAccountService ;

    @Autowired
    JixinManager jixinManager ;

    @Autowired
    RedisHelper redisHelper ;

    @Autowired
    UserThirdAccountService userThirdAccountService ;

    @Value("${gofobao.javaDomain}")
    private String javaDomain ;

    @Value("${gofobao.h5Domain}")
    private String h5Domain ;

    @Value("${gofobao.aliyun-bankinfo-url}")
    String aliyunQueryBankUrl ;

    @Value("${gofobao.aliyun-bankinfo-appcode}")
    String aliyunQueryAppcode ;

    @Override
    public ResponseEntity<VoPreOpenAccountResp> preOpenAccount(Long userId) {
        //1。 验证用户是否存在
        Users user = userService.findById(userId);
        if(ObjectUtils.isEmpty(user)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你访问的账户不存在! ", VoPreOpenAccountResp.class)) ;
        }

        //2. 判断用户是否已经开过存管账户
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId()) ;
        if(!ObjectUtils.isEmpty(userThirdAccount) ){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户已经开户！", VoPreOpenAccountResp.class)) ;
        }


        //3. 判断用户是否绑定手机
        if(StringUtils.isEmpty(user.getPhone())){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户没有绑定手机，请先绑定手机！", VoPreOpenAccountResp.class)) ;
        }


        // 4.查询银行卡
        List<BankAccount> bankAccountList = bankAccountService.listBankByUserId(userId) ;
        List<VoBankResp> voBankResps = new ArrayList<>(bankAccountList.size()) ;
        VoBankResp voBankResp = null ;
        for(BankAccount bankAccount: bankAccountList){
            voBankResp = new VoBankResp() ;
            voBankResps.add(voBankResp);
            voBankResp.setId(bankAccount.getId());
            voBankResp.setBankNo(bankAccount.getAccount());
        }

        VoPreOpenAccountResp voPreOpenAccountResp = VoBaseResp.ok("查询成功", VoPreOpenAccountResp.class);
        voPreOpenAccountResp.setMobile(user.getPhone());
        voPreOpenAccountResp.setIdType("01"); //证件类型：身份证
        voPreOpenAccountResp.setIdNo(user.getCardId());
        voPreOpenAccountResp.setName(user.getRealname());
        voPreOpenAccountResp.setBankList(voBankResps);
        return ResponseEntity.ok(voPreOpenAccountResp) ;
    }

    @Override
    public ResponseEntity<VoOpenAccountResp> openAccount(VoOpenAccountReq voOpenAccountReq, Long userId) {
        // 1.用户用户信息
        Users user = userService.findById(userId);
        if(ObjectUtils.isEmpty(user))
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你访问的账户不存在", VoOpenAccountResp.class)) ;
        // 2. 判断用户是否已经开过存管账户
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId()) ;
        if(!ObjectUtils.isEmpty(userThirdAccount) )
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户已经开户！", VoOpenAccountResp.class)) ;

        // 3. 判断用户是否绑定手机
        if(StringUtils.isEmpty(user.getPhone()))
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户没有绑定手机，请先绑定手机！", VoOpenAccountResp.class)) ;

        // 4.判断用户真实姓名
        if( (!StringUtils.isEmpty(user.getRealname())) && !(voOpenAccountReq.getName().equals(user.getRealname())) )
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的填写真实姓名与系统保存的不一致！", VoOpenAccountResp.class)) ;

        // 5.判断身份证
        if( (!StringUtils.isEmpty(user.getCardId())) && !(voOpenAccountReq.getIdNo().equals(user.getCardId())))
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你的填写身份证号与系统保存的不一致！", VoOpenAccountResp.class)) ;

        // 7.短信验证码验证
        String srvTxCode = null ;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, voOpenAccountReq.getMobile()), null) ;
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, voOpenAccountReq.getMobile()));
        }catch (Exception e){
            log.error("UserThirdBizImpl openAccount get redis exception ", e);
        }

        if(StringUtils.isEmpty(srvTxCode)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取", VoOpenAccountResp.class)) ;
        }

        // 8.提交开户
        AccountOpenPlusRequest request = new AccountOpenPlusRequest() ;
        request.setIdType(IdTypeContant.ID_CARD);
        request.setName(voOpenAccountReq.getName());
        request.setMobile(voOpenAccountReq.getMobile());
        request.setIdNo(voOpenAccountReq.getIdNo());
        request.setAcctUse(AcctUseContant.GENERAL_ACCOUNT);
        request.setAcqRes(String.valueOf(user.getId()));
        request.setLastSrvAuthCode(srvTxCode);
        request.setChannel(ChannelContant.HTML);
        request.setSmsCode(voOpenAccountReq.getSmsCode());
        request.setCardNo(voOpenAccountReq.getCardNo());


        AccountOpenPlusResponse response = jixinManager.send(JixinTxCodeEnum.OPEN_ACCOUNT_PLUS, request, AccountOpenPlusResponse.class);
        if((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))){
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试": response.getRetMsg() ;
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, msg, VoOpenAccountResp.class)) ;
        }


        String logo = null;
        String bankName = null ;
        // 获取银行卡信息
        try {
            String cardNo = voOpenAccountReq.getCardNo() ; // 银行卡
            Map<String, String> params = new HashMap<>();
            params.put("bankcard", cardNo);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", String.format("APPCODE %s", aliyunQueryAppcode));
            String jsonStr = OKHttpHelper.get(aliyunQueryBankUrl, params, headers);
            JsonObject result = new JsonParser().parse(jsonStr).getAsJsonObject();
            int status = result.get("status").getAsInt();
            if (status == 0) {
                JsonObject info = result.get("result").getAsJsonObject();
                logo = info.get("bank").getAsString();
                bankName = info.get("logo").getAsString();

            }

        } catch (Exception e) {
            log.error("开户查询银行卡异常");
        }

        // 8.保存银行存管账户到用户中
        String accountId = response.getAccountId();
        UserThirdAccount entity = new UserThirdAccount() ;
        Date nowDate = new Date() ;
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
        entity.setChannel(Integer.parseInt(ChannelContant.HTML));
        entity.setAcctUse(1);
        entity.setAccountId(accountId);
        entity.setPasswordState(0);
        entity.setCardNoBindState(1);
        entity.setName(voOpenAccountReq.getName());
        entity.setBankLogo(logo);
        entity.setBankName(bankName);
        Long id = userThirdAccountService.save(entity) ;

        //  9.保存用户实名信息
        user.setRealname(voOpenAccountReq.getName());
        user.setCardId(voOpenAccountReq.getIdNo());
        user.setUpdatedAt(nowDate);
        boolean b = userService.updUserById(user);
        if(!b){
            log.error("UserThirdBizImpl openAccount insert db error ");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候重试", VoOpenAccountResp.class)) ;
        }

        VoOpenAccountResp voOpenAccountResp = VoBaseResp.ok("开户成功", VoOpenAccountResp.class);
        voOpenAccountResp.setOpenAccountBankName("江西银行");
        voOpenAccountResp.setAccount(accountId);
        voOpenAccountResp.setName(voOpenAccountReq.getName());
        return ResponseEntity.ok(voOpenAccountResp);
    }

    @Override
    public ResponseEntity<VoHtmlResp> modifyOpenAccPwd(Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId) ;
        if(ObjectUtils.isEmpty(userThirdAccount) ){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "当前账户还未实名", VoHtmlResp.class)) ;
        }

        String html = null ;
        // 判断用户是密码初始化还是
        Integer passwordState = userThirdAccount.getPasswordState();
        if(passwordState.equals(0)){ // 初始化密码
            PasswordSetRequest passwordSetRequest = new PasswordSetRequest() ;
            passwordSetRequest.setMobile(userThirdAccount.getMobile());
            passwordSetRequest.setChannel(ChannelContant.HTML);
            passwordSetRequest.setName(userThirdAccount.getName());
            passwordSetRequest.setAccountId(userThirdAccount.getAccountId());
            passwordSetRequest.setIdType(IdTypeContant.ID_CARD);
            passwordSetRequest.setIdNo(userThirdAccount.getIdNo());
            passwordSetRequest.setAcqRes(String.valueOf(userId));
            passwordSetRequest.setRetUrl(String.format("%s%s", h5Domain, ""));
            passwordSetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/1"));
            html = jixinManager.getHtml(JixinTxCodeEnum.PASSWORD_SET, passwordSetRequest) ;
        }else{ // 重置密码
            PasswordResetRequest passwordResetRequest = new PasswordResetRequest() ;
            passwordResetRequest.setMobile(userThirdAccount.getMobile());
            passwordResetRequest.setChannel(ChannelContant.HTML);
            passwordResetRequest.setName(userThirdAccount.getName());
            passwordResetRequest.setAccountId(userThirdAccount.getAccountId());
            passwordResetRequest.setIdType(IdTypeContant.ID_CARD);
            passwordResetRequest.setIdNo(userThirdAccount.getIdNo());
            passwordResetRequest.setAcqRes(String.valueOf(userId));
            passwordResetRequest.setRetUrl(String.format("%s%s", h5Domain, ""));
            passwordResetRequest.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/third/modifyOpenAccPwd/callback/2"));
            html = jixinManager.getHtml(JixinTxCodeEnum.PASSWORD_RESET, passwordResetRequest) ;
        }

        if(StringUtils.isEmpty(html)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }


        VoHtmlResp voHtmlResp = VoBaseResp.ok("成功", VoHtmlResp.class);
        try {
            voHtmlResp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.error("UserThirdBizImpl modifyOpenAccPwd gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }

        return ResponseEntity.ok(voHtmlResp) ;
    }

    @Override
    public ResponseEntity<String> modifyOpenAccPwdCallback(HttpServletRequest request, HttpServletResponse response, Integer type) {
        Long userId = null ;

        if(type == 1){
            PasswordSetResponse passwordSetResponse = jixinManager.callback(request, new TypeToken<PasswordSetResponse>() {
            });

            if(ObjectUtils.isEmpty(passwordSetResponse)){ return ResponseEntity.badRequest().body("error");}
            if(!JixinResultContants.SUCCESS.equals(passwordSetResponse.getRetCode())){
                log.error("UserThirdBizImpl.modifyOpenAccPwdCallback: 回调出失败");
                return ResponseEntity
                        .badRequest()
                        .body("error");
            }

            userId = Long.parseLong(passwordSetResponse.getAcqRes());
        }else{
            PasswordResetResponse passwordResetResponse= jixinManager.callback(request, new TypeToken<PasswordResetResponse>() {
            });

            if(ObjectUtils.isEmpty(passwordResetResponse)){ return ResponseEntity.badRequest().body("error"); }

            if(!JixinResultContants.SUCCESS.equals(passwordResetResponse.getRetCode())){
                log.error("UserThirdBizImpl.modifyOpenAccPwdCallback: 回调出失败");
                return ResponseEntity
                        .badRequest()
                        .body("error");
            }
            userId = Long.parseLong(passwordResetResponse.getAcqRes());
        }


        if(ObjectUtils.isEmpty(userId)){
            log.error("UserThirdBizImpl modifyOpenAccPwdCallback userId is null");
            return ResponseEntity.badRequest().body("error");
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
            log.error("UserThirdBizImpl modifyOpenAccPwdCallback userThirdAccount is null");
            return ResponseEntity.badRequest().body("error");
        }

        if(userThirdAccount.getPasswordState().equals(1)){
            return ResponseEntity.ok("success") ;
        }

        userThirdAccount.setPasswordState(1);
        userThirdAccount.setUpdateAt(new Date());
        Long id = userThirdAccountService.save(userThirdAccount);
        if(id ==  0){
            log.error("UserThirdBizImpl modifyOpenAccPwdCallback update userThirdAccount is error");
            return ResponseEntity.badRequest().body("error");
        }

        return ResponseEntity.ok("success") ;
    }

    @Override
    public ResponseEntity<String> autoTenderCallback(HttpServletRequest request, HttpServletResponse response) {
        AutoBidAuthPlusResponse autoBidAuthPlusResponse = jixinManager.callback(request, new TypeToken<AutoBidAuthPlusResponse>() {
        });

        if(ObjectUtils.isEmpty(autoBidAuthPlusResponse)){
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        if(!JixinResultContants.SUCCESS.equals(autoBidAuthPlusResponse.getRetCode())){
            log.error("UserThirdBizImpl.autoTenderCallback: 回调出失败");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        Long userId = Long.parseLong(autoBidAuthPlusResponse.getAcqRes());


        if(ObjectUtils.isEmpty(userId)){
            log.error("UserThirdBizImpl autoTenderCallback userId is null");
            return ResponseEntity.badRequest().body("error");
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
            log.error("UserThirdBizImpl autoTenderCallback userThirdAccount is null");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }


        if(userThirdAccount.getAutoTenderState() == 1){
            return ResponseEntity.ok("success") ;
        }

        userThirdAccount.setAutoTenderState(1);
        userThirdAccount.setAutoTenderTotAmount(999999999L);
        userThirdAccount.setAutoTenderTxAmount(999999999L);
        userThirdAccount.setAutoTenderOrderId(autoBidAuthPlusResponse.getOrderId());
        userThirdAccount.setUpdateAt(new Date());
        Long id = userThirdAccountService.save(userThirdAccount);
        if(id ==  0){
            log.error("UserThirdBizImpl autoTenderCallback update userThirdAccount is error");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        return ResponseEntity.ok("success") ;
    }

    @Override
    public ResponseEntity<VoHtmlResp> autoTender(Long userId, String smsCode) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "请先开通江西银行存管账户！", VoHtmlResp.class)) ;
        }

        if( userThirdAccount.getPasswordState() != 1 ){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "请先设置江西银行存管账户交易密码！", VoHtmlResp.class)) ;
        }

        String srvTxCode = null ;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.AUTO_BID_AUTH_PLUS, userThirdAccount.getMobile()), null) ;
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.AUTO_BID_AUTH_PLUS, userThirdAccount.getMobile()));
        }catch (Exception e){
            log.error("UserThirdBizImpl autoTender get redis exception ", e);
        }

        if(StringUtils.isEmpty(srvTxCode)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取", VoHtmlResp.class)) ;
        }

        AutoBidAuthPlusRequest autoBidAuthPlusRequest = new AutoBidAuthPlusRequest() ;
        autoBidAuthPlusRequest.setAccountId(userThirdAccount.getAccountId());
        autoBidAuthPlusRequest.setOrderId(System.currentTimeMillis() + RandomHelper.generateNumberCode(6));
        autoBidAuthPlusRequest.setTxAmount("999999999") ;
        autoBidAuthPlusRequest.setTotAmount("999999999"); ;
        autoBidAuthPlusRequest.setForgotPwdUrl(h5Domain);
        autoBidAuthPlusRequest.setRetUrl(h5Domain);
        autoBidAuthPlusRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/user/third/autoTender/callback"));
        autoBidAuthPlusRequest.setLastSrvAuthCode(srvTxCode);
        autoBidAuthPlusRequest.setSmsCode(smsCode);
        autoBidAuthPlusRequest.setAcqRes(userId.toString());
        autoBidAuthPlusRequest.setChannel(ChannelContant.HTML);
        String html = null;
        try {
            html = jixinManager.getHtml(JixinTxCodeEnum.AUTO_BID_AUTH_PLUS, autoBidAuthPlusRequest);
        } catch (Exception e) {
            log.error("UserThirdBizImpl autoTender get redis exception ", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }

        VoHtmlResp resp = VoBaseResp.ok("请求成功", VoHtmlResp.class) ;
        try {
            resp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.error("UserThirdBizImpl autoTender gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<VoHtmlResp> autoTranfter(Long userId, String smsCode) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "请先开通江西银行存管账户！", VoHtmlResp.class)) ;
        }

        if( userThirdAccount.getPasswordState() != 1 ){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "请先设置江西银行存管账户交易密码！", VoHtmlResp.class)) ;
        }

        if( userThirdAccount.getAutoTenderState() != 1 ){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "请先签约江西银行自动投标协议！", VoHtmlResp.class)) ;
        }


        String srvTxCode = null ;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.AUTO_CREDIT_INVEST_AUTH_PLUS, userThirdAccount.getMobile()), null) ;
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.AUTO_CREDIT_INVEST_AUTH_PLUS, userThirdAccount.getMobile()));
        }catch (Exception e){
            log.error("UserThirdBizImpl autoTranfter get redis exception", e);
        }

        if(StringUtils.isEmpty(srvTxCode)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取", VoHtmlResp.class)) ;
        }

        AutoCreditInvestAuthPlusRequest autoCreditInvestAuthPlusRequest = new AutoCreditInvestAuthPlusRequest() ;
        autoCreditInvestAuthPlusRequest.setAccountId(userThirdAccount.getAccountId());
        autoCreditInvestAuthPlusRequest.setOrderId(System.currentTimeMillis() + RandomHelper.generateNumberCode(6));
        autoCreditInvestAuthPlusRequest.setForgotPwdUrl(h5Domain);
        autoCreditInvestAuthPlusRequest.setRetUrl(h5Domain);
        autoCreditInvestAuthPlusRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/user/third/autoTranfer/callback"));
        autoCreditInvestAuthPlusRequest.setLastSrvAuthCode(srvTxCode);
        autoCreditInvestAuthPlusRequest.setSmsCode(smsCode);
        autoCreditInvestAuthPlusRequest.setAcqRes(userId.toString());
        autoCreditInvestAuthPlusRequest.setChannel(ChannelContant.HTML);


        String html = null;
        try {
            html = jixinManager.getHtml(JixinTxCodeEnum.AUTO_CREDIT_INVEST_AUTH_PLUS, autoCreditInvestAuthPlusRequest);
        } catch (Exception e) {
            log.error("UserThirdBizImpl autoTranfter get redis exception ", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }

        VoHtmlResp resp = VoBaseResp.ok("请求成功", VoHtmlResp.class) ;
        try {
            resp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.error("UserThirdBizImpl autoTender autoTranfter exception", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<String> autoTranferCallback(HttpServletRequest request, HttpServletResponse response) {
        AutoCreditInvestAuthPlusResponse autoCreditInvestAuthPlusResponse = jixinManager.callback(request, new TypeToken<AutoCreditInvestAuthPlusResponse>() {
        });

        if(ObjectUtils.isEmpty(autoCreditInvestAuthPlusResponse)){
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        if(!JixinResultContants.SUCCESS.equals(autoCreditInvestAuthPlusResponse.getRetCode())){
            log.error("UserThirdBizImpl.autoTranferCallback: 回调出失败");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        Long userId = Long.parseLong(autoCreditInvestAuthPlusResponse.getAcqRes());

        if(ObjectUtils.isEmpty(userId)){
            log.error("UserThirdBizImpl autoTranferCallback userId is null");
            return ResponseEntity.badRequest().body("error");
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
            log.error("UserThirdBizImpl autoTranferCallback userThirdAccount is null");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }


        if(userThirdAccount.getAutoTransferState() == 1){
            return ResponseEntity.ok("success") ;
        }

        userThirdAccount.setAutoTransferState(1);
        userThirdAccount.setAutoTransferBondOrderId(autoCreditInvestAuthPlusResponse.getOrderId());
        userThirdAccount.setUpdateAt(new Date());
        Long id = userThirdAccountService.save(userThirdAccount);
        if(id ==  0){
            log.error("UserThirdBizImpl autoTranferCallback update userThirdAccount is error");
            return ResponseEntity
                    .badRequest()
                    .body("error");
        }

        return ResponseEntity.ok("success") ;
    }

    @Override
    public ResponseEntity<VoSignInfoResp> querySigned(Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "请先开通江西银行存管账户！", VoSignInfoResp.class)) ;
        }

        if( userThirdAccount.getPasswordState() != 1 ){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "请先设置江西银行存管账户交易密码！", VoSignInfoResp.class)) ;
        }


        VoSignInfoResp re = VoBaseResp.ok("查询成功", VoSignInfoResp.class);
        re.setAutoTenderState(userThirdAccount.getAutoTenderState() == 1);
        re.setAutoTenderState(userThirdAccount.getAutoTransferState() == 1);
        return ResponseEntity.ok(re) ;
    }

}
