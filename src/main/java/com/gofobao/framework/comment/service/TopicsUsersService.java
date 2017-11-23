package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.vo.response.VoTopicCommentManagerListResp;
import com.gofobao.framework.comment.vo.response.VoTopicListResp;
import com.gofobao.framework.comment.vo.response.VoTopicMemberCenterResp;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

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

    /**
     * 我的帖子
     * @param topicTypeId
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicListResp> listUserTopic(Long topicTypeId, Long userId, Integer pageable,
                                                  HttpServletRequest httpServletRequest);

    /**
     * 评论管理
     * @param sourceType
     * @param httpServletRequest
     * @param pageable
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicCommentManagerListResp> listComment(Integer sourceType, HttpServletRequest httpServletRequest, Integer pageable, Long userId);

    /**
     * 被评论管理
     * @param sourceType
     * @param httpServletRequest
     * @param pageable
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicCommentManagerListResp> listByComment(Integer sourceType, HttpServletRequest httpServletRequest, Integer pageable, Long userId);

    /**
     * 查询用户名是否重复
     * @param username
     * @return
     */
    TopicsUsers findTopByUsername(String username);
}
