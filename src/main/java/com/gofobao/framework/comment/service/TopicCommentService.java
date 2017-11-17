package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.vo.request.VoTopicCommentReq;
import com.gofobao.framework.comment.vo.response.VoTopicCommentListResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by xin on 2017/11/10.
 */
public interface TopicCommentService {

    /**
     * 发布评论
     *
     * @param voTopicCommentReq
     * @param userId
     * @return
     */
    ResponseEntity<VoBaseResp> publishComment(VoTopicCommentReq voTopicCommentReq, Long userId);

    /**
     * 发现评论
     * @param id
     * @return
     */
    TopicComment findById(Long id);

    /**
     * 批量修改冗余数据
     * @param userId
     * @param username
     * @param avatar
     */
    void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception;

    TopicComment save(TopicComment topicComment);

    /**
     * 查询话题评论
     * @param httpServletRequest
     * @param topicId
     * @param pageIndex
     * @return
     */
    ResponseEntity<VoTopicCommentListResp> listDetail(HttpServletRequest httpServletRequest, Long topicId, Integer pageIndex);
}
