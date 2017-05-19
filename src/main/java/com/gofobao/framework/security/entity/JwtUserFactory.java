package com.gofobao.framework.security.entity;

import com.gofobao.framework.member.entity.Users;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(Users user) {
        return new JwtUser(user.getId(),
                user.getCreatedAt(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getPassword(),
                user.getIsLock());
    }
}
