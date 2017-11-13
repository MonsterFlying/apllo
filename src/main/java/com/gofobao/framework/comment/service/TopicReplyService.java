package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.entity.TopicReport;
import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
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

    TopicReply findById(Long id);


}
