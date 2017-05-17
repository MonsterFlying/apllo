package com.gofobao.framework.security.vo;

import com.gofobao.framework.core.helper.PasswordHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by Max on 17/5/17.
 */
@Slf4j
public class ApolloPasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(CharSequence charSequence) {
        log.info(String.format("Spring security passwordEncoder encode %s", charSequence.toString()));
        return charSequence.toString();
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        try {
            return PasswordHelper.verifyPassword(s, charSequence.toString());
        } catch (Exception e) {
            log.error("Spring security passwordEncoder matches exception", e );
            return false ;
        }
    }
}
