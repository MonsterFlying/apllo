package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.TopicsUsers;

public interface TopicsUsersService {

    /**
     * 根据用户Id 获取用户论坛基础信息,
     * <p>
     * 注意:
     * 当查询不存在的用户时, 系统会自动插入一条记录(user表总得username)
     *
     * @param userId
     * @return
     */
    TopicsUsers findByUserId(Long userId) throws Exception;

    /**
     * 保存
     * @param topicsUsers
     * @return
     */
    TopicsUsers save(TopicsUsers topicsUsers);
}
