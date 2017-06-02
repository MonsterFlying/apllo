package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.UserInfo;
import com.gofobao.framework.member.repository.UserInfoRepository;
import com.gofobao.framework.member.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Max on 17/6/1.
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoRepository userInfoRepository;


    @Override
    public UserInfo save(UserInfo userInfo) {
        return userInfoRepository.save(userInfo);
    }
}
