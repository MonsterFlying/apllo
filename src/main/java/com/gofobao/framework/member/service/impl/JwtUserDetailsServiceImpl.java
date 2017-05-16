package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.security.entity.JwtUserFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Created by stephan on 20.03.16.
 */
@Service
@Slf4j
public class JwtUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Users user = userRepository.findOne(Long.parseLong(id)) ;

        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with id '%s'.", id));
        } else {
            return JwtUserFactory.create(user);
        }
    }
}
