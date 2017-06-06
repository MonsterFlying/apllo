package com.gofobao.framework.member.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.member.vo.request.VoOpenAccountReq;
import com.gofobao.framework.member.vo.response.VoPreOpenAccountResp;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 银行存管账户
 * Created by Max on 17/5/22.
 */
public interface UserThirdBiz {


    /**
     * 会员存管开户前置请求
     * @param userId
     * @return
     */
    ResponseEntity<VoPreOpenAccountResp> preOpenAccount(Long userId);

    /**
     * 会员开户
     * @param voOpenAccountReq
     * @param userId
     * @return
     */
    ResponseEntity<VoBaseResp> openAccount(VoOpenAccountReq voOpenAccountReq, Long userId);

    /**
     * 初始化银行存管密码
     * @param userId
     * @return
     */
    ResponseEntity<VoHtmlResp> modifyOpenAccPwd(Long userId);

    /**
     * 银行存管密码回调
     * @param request
     * @param response
     * @param type
     * @return
     */
    ResponseEntity<String> modifyOpenAccPwdCallback(HttpServletRequest request, HttpServletResponse response, Integer type);
}
