package com.gofobao.framework.message.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.vo.VoSmsReq;
import org.springframework.http.ResponseEntity;

import javax.servlet.ServletRequest;

/**
 * Created by Max on 17/5/17.
 */
public interface MessageBiz {

    /**
     * 发送注册短信验证码
     * @param request 请求类
     * @param voSmsReq 消息体
     * @return
     */
    ResponseEntity<VoBaseResp> sendRegisterCode(ServletRequest request, VoSmsReq voSmsReq);
}
