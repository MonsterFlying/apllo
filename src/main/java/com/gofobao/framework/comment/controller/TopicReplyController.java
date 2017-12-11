package com.gofobao.framework.comment.controller;

import com.gofobao.framework.comment.service.TopicReplyService;
import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
import com.gofobao.framework.comment.vo.response.VoTopicReplyListResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by xin on 2017/11/13.
 */
@RestController
public class TopicReplyController {
    @Autowired
    private TopicReplyService topicReplyService;

    @PostMapping("/comment/topic/reply/publish")
    public ResponseEntity<VoBaseResp> publishReply(@Valid @ModelAttribute VoTopicReplyReq voTopicReplyReq,
                                                   @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicReplyService.publishReply(voTopicReplyReq, userId);
    }

    @GetMapping("/comment/topic/reply/list/{topicCommentId}")
    public ResponseEntity<VoTopicReplyListResp> listReply(@PathVariable() Long topicCommentId) {
        return topicReplyService.listReply(topicCommentId);
    }

    @GetMapping("/comment/topic/reply/{topicReplyId}")
    public ResponseEntity<VoBaseResp> delReply(@PathVariable Long topicReplyId,
                                               @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicReplyService.deleteReply(topicReplyId, userId);
    }

}
