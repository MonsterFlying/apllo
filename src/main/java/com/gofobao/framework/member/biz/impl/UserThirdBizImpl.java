package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.api.contants.*;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_open_plus.AccountOpenPlusRequest;
import com.gofobao.framework.api.model.account_open_plus.AccountOpenPlusResponse;
import com.gofobao.framework.api.model.password_reset.PasswordResetRequest;
import com.gofobao.framework.api.model.password_reset.PasswordResetResponse;
import com.gofobao.framework.api.model.password_set.PasswordSetRequest;
import com.gofobao.framework.api.model.password_set.PasswordSetResponse;
import com.gofobao.framework.asset.entity.BankAccount;
import com.gofobao.framework.asset.service.BankAccountService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.VoHtmlResp;
import com.gofobao.framework.member.vo.VoOpenAccountReq;
import com.gofobao.framework.member.vo.response.VoBankResp;
import com.gofobao.framework.member.vo.response.VoPreOpenAccountResp;
import com.google.common.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Override
    public ResponseEntity<VoPreOpenAccountResp> preOpenAccount(Long userId) {
        //1。 验证用户是否存在
        Users user = userService.findById(userId);
        if(ObjectUtils.isEmpty(user)){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "你访问的账户不存在! ", VoPreOpenAccountResp.class)) ;
        }

        //2. 判断用户是否已经开过存管账户
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId()) ;
        if(!ObjectUtils.isEmpty(userThirdAccount) ){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户已经开户！", VoPreOpenAccountResp.class)) ;
        }


        //3. 判断用户是否绑定手机
        if(StringUtils.isEmpty(user.getPhone())){
            return ResponseEntity.badRequest().body( VoBaseResp.error(VoBaseResp.ERROR, "你的账户没有绑定手机，请先绑定手机！", VoPreOpenAccountResp.class)) ;
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
    public ResponseEntity<VoBaseResp> openAccount(VoOpenAccountReq voOpenAccountReq, Long userId) {
        // 1.用户用户信息
        Users user = userService.findById(userId);
        if(ObjectUtils.isEmpty(user))
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "你访问的账户不存在")) ;
        // 2. 判断用户是否已经开过存管账户
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId()) ;
        if(!ObjectUtils.isEmpty(userThirdAccount) )
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户已经开户！")) ;

        // 3. 判断用户是否绑定手机
        if(StringUtils.isEmpty(user.getPhone()))
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "你的账户没有绑定手机，请先绑定手机！")) ;

        // 4.判断用户真实姓名
        if( (!StringUtils.isEmpty(user.getRealname())) && !(voOpenAccountReq.getName().equals(user.getRealname())) )
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "你的填写真实姓名与系统保存的不一致！")) ;

        // 5.判断身份证
        if( (!StringUtils.isEmpty(user.getCardId())) && !(voOpenAccountReq.getIdNo().equals(user.getCardId())))
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "你的填写身份证号与系统保存的不一致！")) ;

        // 6.银行卡号
        if(!ObjectUtils.isEmpty(voOpenAccountReq.getBankId())){
           BankAccount bankAccount = bankAccountService.findByUserIdAndId(userId, voOpenAccountReq.getBankId()) ;
           if(!bankAccount.getAccount().equals(voOpenAccountReq.getCardNo())){
               return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "你的填写银行卡号与系统保存的不一致！")) ;
           }
        }

        // 7.短信验证码验证
        String srvTxCode = null ;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, voOpenAccountReq.getMobile()), null) ;
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, voOpenAccountReq.getMobile()));
        }catch (Exception e){
            log.error("UserThirdBizImpl openAccount get redis exception ", e);
        }

        if(StringUtils.isEmpty(srvTxCode)){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码已过期，请重新获取")) ;
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
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg)) ;
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
        Long id = userThirdAccountService.save(entity) ;

        //  9.保存用户实名信息
        user.setRealname(voOpenAccountReq.getName());
        user.setCardId(voOpenAccountReq.getIdNo());
        user.setUpdatedAt(nowDate);
        boolean b = userService.updUserById(user);
        if(!b){
            log.error("UserThirdBizImpl openAccount insert db error ");
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候重试")) ;
        }

        return ResponseEntity.ok(VoBaseResp.ok("开户成功"));
    }

    @Override
    public ResponseEntity<VoHtmlResp> modifyOpenAccPwd(Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId) ;
        if(ObjectUtils.isEmpty(userThirdAccount) ){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,  "当前账户还未实名", VoHtmlResp.class)) ;
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
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }


        VoHtmlResp voHtmlResp = VoBaseResp.ok("成功", VoHtmlResp.class);
        try {
            voHtmlResp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.error("UserThirdBizImpl modifyOpenAccPwd gethtml exceptio", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }

        return ResponseEntity.ok(voHtmlResp) ;
    }

    @Override
    public ResponseEntity<String> modifyOpenAccPwdCallback(HttpServletRequest request, HttpServletResponse response, Integer type) {
        Long userId = null ;

        if(type == 1){
            PasswordSetResponse passwordSetResponse = jixinManager.callback(request, new TypeToken<PasswordSetResponse>() {
            });

            if(ObjectUtils.isEmpty(passwordSetResponse)){
                return ResponseEntity.badRequest().body("error");
            }

            if(!JixinResultContants.SUCCESS.equals(passwordSetResponse.getRetCode())){
                log.error("回调出失败");
                return ResponseEntity.ok("success") ;
            }

            userId = Long.parseLong(passwordSetResponse.getAcqRes());


        }else{
            PasswordResetResponse passwordResetResponse= jixinManager.callback(request, new TypeToken<PasswordResetResponse>() {
            });

            if(ObjectUtils.isEmpty(passwordResetResponse)){
                return ResponseEntity.badRequest().body("error");
            }

            if(!JixinResultContants.SUCCESS.equals(passwordResetResponse.getRetCode())){
                log.error("回调出失败");
                return ResponseEntity.ok("success") ;
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
}
