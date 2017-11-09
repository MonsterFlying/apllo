package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.vo.request.VoTopicReq;
import com.gofobao.framework.comment.vo.response.VoTopicListResp;
import com.gofobao.framework.comment.vo.response.VoTopicResp;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by xin on 2017/11/8.
 */
public interface TopicService {
    /**
     * 发布主题
     * @param voTopicReq
     * @param userId
     * @param httpServletRequest
     * @return
     */
    ResponseEntity<VoTopicResp> publishTopic(VoTopicReq voTopicReq, Long userId, HttpServletRequest httpServletRequest);

    /**
     * 删除主题
     * @param id
     * @return
     */
    ResponseEntity<VoTopicResp> delTopic(long id,long userId);

    /**
     * 查询板块下的帖子
     * @param topicTypeId
     * @return
     */
    ResponseEntity<VoTopicListResp> listTopic(Integer topicTypeId);
}
