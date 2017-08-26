package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.contants.SrvTxCodeContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.mobile_modify_plus.MobileModifyRequest;
import com.gofobao.framework.api.model.mobile_modify_plus.MobileModifyResponse;
import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.MacthHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.request.VoBindPhone;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhoneReq;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.member.vo.response.VoOpenAccountResp;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
@Slf4j
public class UserPhoneBizImpi implements UserPhoneBiz {

    @Autowired
    UserService userService;

    @Autowired
    MacthHelper macthHelper;

    @Autowired
    UserBiz userBiz;

    @Autowired
    UserThirdAccountService userThirdAccountService ;

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    JixinManager jixinManager;


    /**
     * 更换手机绑定
     *
     * @param voBindSwitchPhoneReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBasicUserInfoResp> bindSwitchPhone(VoBindSwitchPhoneReq voBindSwitchPhoneReq) {
        Long userId = voBindSwitchPhoneReq.getUserId();
        String newPhone = voBindSwitchPhoneReq.getNewPhone();


        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users) || ObjectUtils.isEmpty(users.getPhone())) {
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前登录账号不可用，详情请联系管理员！", VoBasicUserInfoResp.class));
        }
        // 验证新手机是否
        boolean noExists = userService.notExistsByPhone(newPhone);
        if(!noExists){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "新手机号已在平台注册,请使用其他手机号!", VoBasicUserInfoResp.class));
        }

        String phone = users.getPhone();
        boolean bool =  macthHelper.match( MqTagEnum.SMS_SWICTH_PHONE.getValue(), phone, voBindSwitchPhoneReq.getNewPhoneCaptcha()) ;
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "原手机验证码错误/或者已过期，请重新发送短信验证码!", VoBasicUserInfoResp.class));
        }

        // 修改成验证即信手机
        String srvTxCode = null;
        try {
            srvTxCode = redisHelper.get(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, voBindSwitchPhoneReq.getNewPhone()), null);
            redisHelper.remove(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, voBindSwitchPhoneReq.getNewPhone()));
        } catch (Throwable e) {
            log.error("UserThirdBizImpl opeanAccountCallBack get redis exception ", e);
        }

        if (StringUtils.isEmpty(srvTxCode)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "新手机验证码错误/或者已过期，请重新发送短信验证码!", VoBasicUserInfoResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "UserPhoneBizImpl.bindSwitchPhone: userThirdAccount is null") ;

        // 请求即信修改手机
        MobileModifyRequest mobileModifyRequest = new MobileModifyRequest() ;
        mobileModifyRequest.setAccountId(userThirdAccount.getAccountId());
        mobileModifyRequest.setMobile(voBindSwitchPhoneReq.getNewPhone());
        mobileModifyRequest.setSmsCode(voBindSwitchPhoneReq.getNewPhoneCaptcha());
        mobileModifyRequest.setLastSrvAuthCode(srvTxCode);
        MobileModifyResponse mobileModifyResponse = jixinManager.send(JixinTxCodeEnum.MOBILE_MODIFY_PLUS, mobileModifyRequest, MobileModifyResponse.class);
        if(ObjectUtils.isEmpty(mobileModifyResponse) || !JixinResultContants.SUCCESS.equals(mobileModifyResponse.getRetCode())){
            String msg = ObjectUtils.isEmpty(mobileModifyResponse) ? "网络异常, 请稍后重试!" : mobileModifyResponse.getRetMsg() ;
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, msg, VoBasicUserInfoResp.class));
        }

        users.setPhone(voBindSwitchPhoneReq.getNewPhone()) ;
        users.setUpdatedAt(new Date()) ;
        userService.save(users) ;

        userThirdAccount.setMobile(voBindSwitchPhoneReq.getNewPhone()) ;
        userThirdAccount.setUpdateAt(new Date());
        userThirdAccountService.save(userThirdAccount) ;
        return userBiz.getUserInfoResp(users) ;
    }

    @Override
    public ResponseEntity<VoBaseResp> checkOnlyForUserInfo(VoJudgmentAvailableReq voJudgmentAvailableReq) {
        String msg = "查询成功" ;
        boolean flag = false ;

        if("1".equalsIgnoreCase(voJudgmentAvailableReq.getCheckType())){
            flag = !userService.notExistsByPhone(voJudgmentAvailableReq.getCheckValue()) ;
            if(flag){
                msg = "手机号已在平台注册！" ;
            }
        }else if("2".equalsIgnoreCase(voJudgmentAvailableReq.getCheckType())){
            flag = !userService.notExistsByEmail(voJudgmentAvailableReq.getCheckValue()) ;
            if(flag){
                msg = "邮箱已在平台注册！" ;
            }
        }else{
            flag = !userService.notExistsByUserName(voJudgmentAvailableReq.getCheckValue()) ;
            if(flag){
                msg = "用户名已在平台注册！" ;
            }
        }

        if(flag){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }else{
            return ResponseEntity.ok(VoBaseResp.ok(msg));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBasicUserInfoResp> bindPhone(VoBindPhone voBindPhone, Long userId) {
        // 判断账户手机号是否绑定状态
        Users users = userService.findByIdLock(userId);
        if(ObjectUtils.isEmpty(users)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户不存在!", VoBasicUserInfoResp.class));
        }

        if(users.getIsLock()){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于锁定状态!", VoBasicUserInfoResp.class));
        }

        if(!StringUtils.isEmpty(users.getPhone())){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, " 账户已经绑定手机!", VoBasicUserInfoResp.class));
        }

        // 验证手机号唯一性
        boolean noExists = userService.notExistsByPhone(voBindPhone.getPhone());
        if(!noExists){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机已在平台注册!", VoBasicUserInfoResp.class));
        }

        // 验证短信验证码
        boolean bool = macthHelper.match( MqTagEnum.SMS_BUNDLE.getValue(), voBindPhone.getPhone(), voBindPhone.getSmsCode()) ;
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, " 短信验证码错误或者过期,请重新获取短信验证码!", VoBasicUserInfoResp.class));
        }

        // 保存手机号
        users.setPhone(voBindPhone.getPhone());
        users.setUpdatedAt(new Date());
        userService.save(users) ;
        return userBiz.getUserInfoResp(users) ;
    }

    @Override
    public ResponseEntity<VoBaseResp> verfyUnBindPhoneMessage(Long userId, String smsCode) {
        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users) || ObjectUtils.isEmpty(users.getPhone())) {
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前登录账号不可用，详情请联系管理员！"));
        }

        String phone = users.getPhone();

        if(StringUtils.isEmpty(phone)){
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先绑定手机!"));
        }

        boolean bool =  macthHelper.matchAndNoRemove( MqTagEnum.SMS_SWICTH_PHONE.getValue(), phone, smsCode) ;
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "原手机验证码错误/或者已过期，请重新发送短信验证码!"));
        }


        return ResponseEntity.ok(VoBaseResp.ok("通过"));
    }
}
