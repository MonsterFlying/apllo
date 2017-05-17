package com.gofobao.framework.member.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by admin on 2017/5/17.
 */

@RestController
@RequestMapping("/user/password")
public class PasswordController {

/*    @Autowired
    private PasswordRepository userPasswordSetDao;*/

    /**
     * 密码设置
     */
    @PostMapping("/passwordSet")
    public  void passwordSet(HttpServletRequest  request, Integer userId){






    }



}
