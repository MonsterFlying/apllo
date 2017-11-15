package com.gofobao.framework.comment.controller;

import com.gofobao.framework.comment.biz.TopicsIntegralRecordBiz;
import com.gofobao.framework.comment.vo.response.VoTopicMemberIntegralResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 帖子积分管理
 */
@RestController
public class TopicsIntegralController {


    @Autowired
    TopicsIntegralRecordBiz topicsIntegralRecordBiz ;

    @ApiOperation("我的积分")
    @GetMapping("/member-integral/")
    public ResponseEntity<VoTopicMemberIntegralResp> memberIntegral(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicsIntegralRecordBiz.memberIntegral(userId);
    }
}
