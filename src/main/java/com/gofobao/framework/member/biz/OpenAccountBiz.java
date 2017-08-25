package com.gofobao.framework.member.biz;

import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
/**
 * 开户逻辑类
 */
public interface OpenAccountBiz {

    /**
     * 开户流程:
     *
     * @param userId
     * @param httpServletRequest
     * @param model
     * @return
     */
    String openAccount(Long userId, HttpServletRequest httpServletRequest, Model model);
}
