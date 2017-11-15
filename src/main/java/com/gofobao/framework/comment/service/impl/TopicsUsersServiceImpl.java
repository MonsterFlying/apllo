package com.gofobao.framework.comment.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.repository.TopicsUsersRepository;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.helper.RandomUtil;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class TopicsUsersServiceImpl implements TopicsUsersService {

    @Autowired
    TopicsUsersRepository topicsUsersRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService ;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TopicsUsers findByUserId(Long userId) throws Exception {
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "find user record is empty");
        Specification<TopicsUsers> specification = Specifications
                .<TopicsUsers>and()
                .eq("userId", userId)
                .build();
        TopicsUsers topicsUsers = topicsUsersRepository.findOne(specification);
        if (ObjectUtils.isEmpty(topicsUsers)) {
            // 判断用户是否开户
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
            if(ObjectUtils.isEmpty(userThirdAccount)){
                throw new Exception("禁止未开户用户操作社区") ;
            }
            Date nowDate = new Date();
            TopicsUsers save = new TopicsUsers();
            String username = users.getUsername();
            if (StringUtils.isEmpty(username)) {
                username = "a_z_" + RandomUtil.getRandomString(5) + userId;
            }
            save.setUsername(username);
            save.setAvatar(users.getAvatarPath());
            save.setUserId(userId);
            save.setLevelId(1L);
            save.setCreateDate(nowDate);
            save.setUpdateDate(nowDate);
            save.setForceState(0);
            save.setNoUseIntegral(0L);
            save.setUseIntegral(0L);
            return topicsUsersRepository.save(save);
        } else {
            return topicsUsers;
        }
    }

    @Override
    public TopicsUsers save(@NonNull TopicsUsers topicsUsers) {
        return topicsUsersRepository.save(topicsUsers);
    }
}
