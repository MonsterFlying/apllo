package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.vo.response.VoAccountStatusResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; /**
 * 开户逻辑类
 */
public interface OpenAccountBiz {

    String opeanAccountCallBack(Long userId,
                                String process,
                                HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Model model);

    /**
     *  同步密码问题
     * @param userThirdAccount
     * @return
     */
    boolean findPasswordStateIsInitByUserId(UserThirdAccount userThirdAccount) ;

    /**
     * 存管交易密码管理
     * @param httpServletRequest
     * @param userId
     * @return
     */
    ResponseEntity<VoHtmlResp> accountPasswordManagement(HttpServletRequest httpServletRequest, Long userId);

    /**
     * 平台投标授权
     * @param httpServletRequest
     * @param msgCode
     * @param userId
     * @return
     */
    ResponseEntity<VoHtmlResp> acocuntAuthorizeTender(HttpServletRequest httpServletRequest, String msgCode, Long userId);

    /**
     * 债权转让授权
     * @param httpServletRequest
     * @param msgCode
     * @param userId
     * @return
     */
    ResponseEntity<VoHtmlResp> acocuntAuthorizeTransfer(HttpServletRequest httpServletRequest, String msgCode, Long userId);

    /**
     * 查询用户开户信息
     * @param httpServletRequest
     * @param userId
     * @return
     */
    ResponseEntity<VoAccountStatusResp> acocuntConfigState(HttpServletRequest httpServletRequest, Long userId);
}
