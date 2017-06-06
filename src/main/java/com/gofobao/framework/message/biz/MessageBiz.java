package com.gofobao.framework.message.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.vo.VoAnonSmsReq;
import com.gofobao.framework.message.vo.VoUserSmsReq;
import org.springframework.http.ResponseEntity;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Max on 17/5/17.
 */
public interface MessageBiz {

    /**
     * 发送注册短信验证码
     * @param request 请求类
     * @param voAnonSmsReq 消息体
     * @return
     */
    ResponseEntity<VoBaseResp> sendRegisterCode(ServletRequest request, VoAnonSmsReq voAnonSmsReq);


    /**
     * 发送忘记密码短信验证码
     * @param request 请求类
     * @param voAnonSmsReq 消息体
     * @return
     */
    ResponseEntity<VoBaseResp> sendFindPassword(HttpServletRequest request, VoAnonSmsReq voAnonSmsReq);

    /**
     * 发送更换手机号码短信验证码
     * @param request 请求类
     * @param voUserSmsReq 消息体
     * @return
     */
    ResponseEntity<VoBaseResp> sendSwitchPhone(HttpServletRequest request, VoUserSmsReq voUserSmsReq);

    /**
     * 发送更换手机号码短信验证码
     * @param request 请求类
     * @param voAnonSmsReq 消息体
     * @param userId
     * @return
     */
    ResponseEntity<VoBaseResp> sendBindPhone(HttpServletRequest request, VoAnonSmsReq voAnonSmsReq, Long userId);


    /**
     * 发送开户手机短息
     * @param voUserSmsReq
     * @return
     */
    ResponseEntity<VoBaseResp> openAccount(VoUserSmsReq voUserSmsReq);


    /**
     * 开通自动投标协议短信
     * @param voUserSmsReq
     * @return
     */
    ResponseEntity<VoBaseResp> openAutoTender(VoUserSmsReq voUserSmsReq);

}
