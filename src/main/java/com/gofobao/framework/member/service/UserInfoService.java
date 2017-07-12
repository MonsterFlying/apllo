package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.UserInfo;

/**
 * Created by Max on 17/6/1.
 */
public interface UserInfoService {

    UserInfo save(UserInfo userInfo);

    /**
     * 用户扩展信息
     * @param userId
     * @return
     */
    UserInfo info(Long userId);
}
