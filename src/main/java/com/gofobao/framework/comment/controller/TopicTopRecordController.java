package com.gofobao.framework.comment.controller;

import com.gofobao.framework.comment.biz.TopicTopRecordBiz;
import com.gofobao.framework.comment.vo.request.VoTopicTopReq;
import com.gofobao.framework.comment.vo.response.VoTopicTopRecordResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
public class TopicTopRecordController {

    @Autowired
    TopicTopRecordBiz topicTopRecordBiz;

    @ApiOperation("点赞/取消点赞")
    @PostMapping("/topic-top-record/top-or-cancel")
    public ResponseEntity<VoTopicTopRecordResp> topOrCancelTop(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                               @Valid @ModelAttribute VoTopicTopReq voTopicTopReq) {
        try {
            return topicTopRecordBiz.topOrCancelTop(userId, voTopicTopReq);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage(), VoTopicTopRecordResp.class));
        }
    }
}
