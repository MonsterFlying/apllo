package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.vo.request.VoOpenAccountReq;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.member.vo.response.VoOpenAccountResp;
import com.gofobao.framework.member.vo.response.VoPreOpenAccountResp;
import com.gofobao.framework.member.vo.response.VoSignInfoResp;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

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
    ResponseEntity<VoOpenAccountResp> openAccount(VoOpenAccountReq voOpenAccountReq, Long userId, HttpServletRequest httpServletRequest);

    /**
     * 初始化银行存管密码
     *
     * @param httpServletRequest
     * @param userId
     * @return
     */
    ResponseEntity<VoHtmlResp> modifyOpenAccPwd(HttpServletRequest httpServletRequest, Long userId);

    /**
     * 银行存管密码回调
     * @param request
     * @param response
     * @param type
     * @return
     */
    ResponseEntity<String> modifyOpenAccPwdCallback(HttpServletRequest request, HttpServletResponse response, Integer type);


    /**
     * 自动投标回调
     * @param request
     * @param response
     * @return
     */
    ResponseEntity<String> autoTenderCallback(HttpServletRequest request, HttpServletResponse response);

    /**
     * 自动投标签约
     *
     * @param httpServletRequest
     * @param userId 用户Id
     * @return
     */
    ResponseEntity<VoHtmlResp> autoTender(HttpServletRequest httpServletRequest, Long userId);


    /**
     * 自动转让
     *
     * @param httpServletRequest
     * @param userId
     * @return
     */
    ResponseEntity<VoHtmlResp> autoTranfter(HttpServletRequest httpServletRequest, Long userId);


    /**
     * 自动转让回调
     * @param request
     * @param response
     * @return
     */
    ResponseEntity<String> autoTranferCallback(HttpServletRequest request, HttpServletResponse response);

    /**
     * 查询签约状态
     * @param userId
     * @return
     */
    ResponseEntity<VoSignInfoResp> querySigned(Long userId);

    /**
     * 设置密码回调
     * @param id
     * @param model
     * @return
     */
    String showPassword(Long id, Model model);

    /**
     * 银行存管协议
     * @param userId
     * @param model
     */
    void thirdAccountProtocol(Long userId, Model model);

    /**
     * 自动投标签约回调
     * @param id
     * @param model
     * @return
     */
    String showAutoTender(Long id, Model model);

    String showAutoTranfer(Long id, Model model);

    ResponseEntity<String> publicPasswordModify(HttpServletRequest httpServletRequest, String encode, String channel);

    /**
     *  同步协议状态
     * @param userThirdAccount
     */
    UserThirdAccount synCreditQuth(UserThirdAccount userThirdAccount);
}
