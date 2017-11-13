package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.vo.request.VoTopicCommentReq;
import com.gofobao.framework.comment.vo.response.VoTopicCommentListResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * Created by xin on 2017/11/10.
 */
public interface TopicCommentService {
    /**
     * 查询话题评论
     * @param topicId
     * @param pageable
     * @return
     */
    ResponseEntity<VoTopicCommentListResp> listDetail(long topicId, Pageable pageable);

    /**
     * 发布评论
     * @param voTopicCommentReq
     * @param userId
     * @return
     */
    ResponseEntity<VoBaseResp> publishComment(VoTopicCommentReq voTopicCommentReq, Long userId);
}
