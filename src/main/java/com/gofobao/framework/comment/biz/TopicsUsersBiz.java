package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.vo.response.VoTopicMemberCenterResp;
import org.springframework.http.ResponseEntity;

public interface TopicsUsersBiz {
    /**
     * 论坛用户中心
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicMemberCenterResp> memberCenter(Long userId);
}
