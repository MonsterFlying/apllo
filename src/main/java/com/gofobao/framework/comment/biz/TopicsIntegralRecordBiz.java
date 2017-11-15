package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.vo.response.VoTopicMemberIntegralResp;
import org.springframework.http.ResponseEntity;

public interface TopicsIntegralRecordBiz {
    /**
     * 我的积分
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicMemberIntegralResp> memberIntegral(Long userId);
}
