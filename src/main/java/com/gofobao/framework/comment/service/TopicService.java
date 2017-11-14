package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.vo.request.VoTopicReq;
import com.gofobao.framework.comment.vo.response.VoTopicListResp;
import com.gofobao.framework.comment.vo.response.VoTopicResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.data.domain.Pageable;
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
    ResponseEntity<VoBaseResp> publishTopic(VoTopicReq voTopicReq, Long userId, HttpServletRequest httpServletRequest);

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
    ResponseEntity<VoTopicListResp> listTopic(long topicTypeId, Pageable pageable);

    /**
     * 查询帖子详情
     * @param topicId
     * @return
     */
    ResponseEntity<VoTopicResp> findTopic(long topicId);

    Topic findById(Long soucreId);

    /**
     * 批量修改帖子冗余的用户名和头像
     * @param userId
     * @param username
     * @param avatar
     */
    void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception;

    Topic save(Topic topic);
}
