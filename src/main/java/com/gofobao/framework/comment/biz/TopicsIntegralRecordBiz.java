package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.vo.response.VoTopicIntegralListResp;
import com.gofobao.framework.comment.vo.response.VoTopicMemberIntegralResp;
import org.springframework.http.ResponseEntity;

public interface TopicsIntegralRecordBiz {
    /**
     * 我的积分
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicMemberIntegralResp> memberIntegral(Long userId);

    /**
     * 查询积分记录
     *
     * @param pageInde
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicIntegralListResp> list(Integer pageInde, Long userId);
}
