package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.vo.request.VoUpdateUsernameReq;
import com.gofobao.framework.comment.vo.response.VoAvatarResp;
import com.gofobao.framework.comment.vo.response.VoTopicMemberCenterResp;
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
}
