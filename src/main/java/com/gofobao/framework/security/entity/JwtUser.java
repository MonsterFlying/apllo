package com.gofobao.framework.security.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

/**
 * JWT 用户
 * Created by Max on 2017/5/17.
 */
@Data
@NoArgsConstructor
public class JwtUser implements UserDetails{
    private Long id;
    private Date updateAt ;
    private String username ;
    private String phone ;
    private String email ;
    private String password ;
    private Boolean isLock ;


    public JwtUser(Long id, Date updateAt, String username, String phone, String email, String password, Boolean isLock) {
        this.id = id;
        this.updateAt = updateAt;
        String temp = username ;
        if(Objects.isNull(temp)) temp = phone ;
        if(Objects.isNull(temp)) temp = email ;
        this.username = temp ;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.isLock = isLock;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList() ;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isLock == false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
