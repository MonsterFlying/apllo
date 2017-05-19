package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.UserCache;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface UserCacheService {

    /**
     * 根据id查询UserCache
     * @param id
     * @return
     */
    UserCache findById(Long id);
}
