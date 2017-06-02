package com.gofobao.framework.listener.providers;

import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Created by Max on 17/6/1.
 */
@Component
@Slf4j
public class UserActiveProvider {
    @Autowired
    UserService userService;

    /**
     * 注册活动
     * @param body
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean  registerActive( Map<String, String> body){
        // 发送体验金

        // 增加统计




        return true;
    }
}
