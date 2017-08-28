package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.entity.UserThirdAccount;
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
}
