package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.UserInfo;
import com.gofobao.framework.member.repository.UserInfoRepository;
import com.gofobao.framework.member.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/**
 * Created by Max on 17/6/1.
 */
@Component
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserInfo save(UserInfo userInfo) {
        return userInfoRepository.save(userInfo);
    }

    @Override
    public UserInfo info(Long userId) {
        return userInfoRepository.findOne(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo update(UserInfo userInfo) {

        return entityManager.merge(userInfo);
    }
}
