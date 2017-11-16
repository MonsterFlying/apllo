package com.gofobao.framework.comment.controller;

import com.gofobao.framework.comment.service.TopicCommentService;
import com.gofobao.framework.comment.vo.request.VoTopicCommentReq;
import com.gofobao.framework.comment.vo.response.VoTopicCommentListResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by xin on 2017/11/10.
 */
@RestController
public class TopicCommentController {

    @Autowired
    private TopicCommentService topicCommentService;

    @GetMapping("/comment/topic/detail/{topicId}")
    public ResponseEntity<VoTopicCommentListResp> listComment(@PathVariable long topicId, Pageable pageable) {
        return topicCommentService.listDetail(topicId, pageable);
    }

    @PostMapping("/comment/topic/comment/publish")
    public ResponseEntity<VoBaseResp> publishComment(@Valid @ModelAttribute VoTopicCommentReq voTopicCommentReq,
                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicCommentService.publishComment(voTopicCommentReq, userId);
    }
}
