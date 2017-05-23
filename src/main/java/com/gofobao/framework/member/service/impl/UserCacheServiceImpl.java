package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.repository.UserCacheRepository;
import com.gofobao.framework.member.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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

    public boolean insert(UserCache userCache){
        if (ObjectUtils.isEmpty(userCache)){
            return false;
        }
        userCache.setUserId(null);
        return !ObjectUtils.isEmpty(userCacheRepository.save(userCache));
    }

    public boolean update(UserCache userCache){
        if (ObjectUtils.isEmpty(userCache) || ObjectUtils.isEmpty(userCache.getUserId())){
            return false;
        }
        return !ObjectUtils.isEmpty(userCacheRepository.save(userCache));
    }
}
