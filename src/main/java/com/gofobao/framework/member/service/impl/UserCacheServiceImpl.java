package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.repository.UserCacheRepository;
import com.gofobao.framework.member.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
public class UserCacheServiceImpl implements UserCacheService{

    @Autowired
    private UserCacheRepository userCacheRepository;

    /**
     * 根据id查询UserCache
     * @param id
     * @return
     */
    public UserCache findById(Long id){
        return userCacheRepository.findOne(id);
    }

    public UserCache findByUserIdLock(Long userId){
        return userCacheRepository.findByUserId(userId);
    }

    public UserCache save(UserCache userCache){ return userCacheRepository.save(userCache); }

    public UserCache updateById(UserCache userCache){ return userCacheRepository.save(userCache); }
}
