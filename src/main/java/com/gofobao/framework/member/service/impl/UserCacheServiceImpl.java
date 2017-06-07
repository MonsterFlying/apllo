package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.repository.UserCacheRepository;
import com.gofobao.framework.member.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
public class UserCacheServiceImpl implements UserCacheService {

    @Autowired
    private UserCacheRepository userCacheRepository;

    /**
     * 根据id查询UserCache
     *
     * @param id
     * @return
     */
    public UserCache findById(Long id) {
        return userCacheRepository.findOne(id);
    }

    public UserCache findByUserIdLock(Long userId) {
        return userCacheRepository.findByUserId(userId);
    }

    public UserCache save(UserCache userCache) {
        return userCacheRepository.save(userCache);
    }

    public UserCache updateById(UserCache userCache) {
        return userCacheRepository.save(userCache);
    }

    public List<UserCache> findList(Specification<UserCache> specification) {
        return userCacheRepository.findAll(specification);
    }

    public List<UserCache> findList(Specification<UserCache> specification, Sort sort) {
        return userCacheRepository.findAll(specification, sort);
    }

    public List<UserCache> findList(Specification<UserCache> specification, Pageable pageable) {
        return userCacheRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<UserCache> specification) {
        return userCacheRepository.count(specification);
    }
}
