package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.entity.TopicReport;
import com.gofobao.framework.comment.repository.TopicReplyRepository;
import com.gofobao.framework.comment.service.TopicReplyService;
import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by xin on 2017/11/13.
 */
@Service
public class TopicReplyServiceImpl implements TopicReplyService {

    @Autowired
    TopicReplyRepository topicReplyRepository;

    @Override
    public ResponseEntity<VoBaseResp> publishReply(VoTopicReplyReq voTopicReplyReq, Long userId) {
        return null;
    }

    @Override
    public TopicReply findById(Long id) {
        return topicReplyRepository.findOne(id);
    }


}
