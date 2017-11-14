package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
import com.gofobao.framework.comment.vo.response.VoTopicReplyListResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by xin on 2017/11/13.
 */
public interface TopicReplyService {
    /**
     * 发表回复
     * @param voTopicReplyReq
     * @param userId
     * @return
     */
    ResponseEntity<VoBaseResp> publishReply(VoTopicReplyReq voTopicReplyReq, Long userId);

    /**
     * 查询回复
     * @param topicCommentId
     * @return
     */
    ResponseEntity<VoTopicReplyListResp> listReply(Long topicCommentId);
}
