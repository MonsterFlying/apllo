package com.gofobao.framework;

import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TokenGenerateTest {

    @Autowired
    UserService userService;

    @Autowired
    JwtTokenHelper jwtTokenHelper ;

    /**
     * 为方便测试生成jwt token
     */
    @Test
    public void generateTokenTest() {
        final long userId = 44914L;
        Users user = userService.findById(userId);
        String username = user.getUsername();
        if (StringUtils.isEmpty(username)) username = user.getPhone();
        if (StringUtils.isEmpty(username)) username = user.getEmail();
        user.setUsername(username);
        Integer requestSource = 1;

        final String token = jwtTokenHelper.generateToken(user, requestSource);
        log.info("Authorization :  Bearer " + token);
    }

}
