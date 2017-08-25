package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.member.biz.OpenAccountBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

@Service
public class OpenAccountBizImpl implements OpenAccountBiz {

    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String openAccount(Long userId, HttpServletRequest httpServletRequest, Model model) {
        Users user = userService.findById(userId);
        Preconditions.checkNotNull(user, "OpenAccountBizImpl") ;

        return null;
    }
}
