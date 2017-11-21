package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.vo.request.VoUpdateUsernameReq;
import com.gofobao.framework.comment.vo.response.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface TopicsUsersBiz {
    /**
     * 论坛用户中心
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicMemberCenterResp> memberCenter(Long userId);

    /**
     * 修改头像
     * @param httpServletRequest
     * @param userId
     * @return
     */
    ResponseEntity<VoAvatarResp> avatar(HttpServletRequest httpServletRequest, Long userId);

    /**
     *  更改用户
     * @param voUpdateUsernameReq
     * @param userId
     * @return
     */
    ResponseEntity<VoBaseResp> updateUsername(VoUpdateUsernameReq voUpdateUsernameReq, Long userId);

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
}
