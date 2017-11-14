package com.gofobao.framework.comment.controller;

import com.gofobao.framework.comment.biz.TopicReportBiz;
import com.gofobao.framework.comment.vo.request.VoTopicReportReq;
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
public class TopicReportController {

    @Autowired
    TopicReportBiz topicReportBiz;

    @ApiOperation("举报")
    @PostMapping("/topic-report/report")
    public ResponseEntity<VoBaseResp> topOrCancelTop(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                     @Valid @ModelAttribute VoTopicReportReq voTopicReportReq) {
        try {
            return topicReportBiz.report(userId, voTopicReportReq);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage()));
        }
    }
}
