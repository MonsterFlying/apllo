package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.service.TopicReplyService;
import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by xin on 2017/11/13.
 */
@Service
public class TopicReplyServiceImpl implements TopicReplyService {

    /**
     * 发表回复
     *
     * @param voTopicReplyReq
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> publishReply(VoTopicReplyReq voTopicReplyReq, Long userId) {
        return ResponseEntity.ok(VoBaseResp.ok("回复成功!"));
    }
}
