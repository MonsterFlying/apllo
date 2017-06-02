package com.gofobao.framework.member.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface UserBiz {

    /**
     * 用户注册
     * @param request 请求
     * @param voRegisterReq 注册实体
     * @return
     */
    ResponseEntity<VoBaseResp> register(HttpServletRequest request, VoRegisterReq voRegisterReq) throws Exception;
}

