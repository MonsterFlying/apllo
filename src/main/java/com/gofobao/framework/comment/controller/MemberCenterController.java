package com.gofobao.framework.comment.controller;

import com.gofobao.framework.comment.biz.TopicsUsersBiz;
import com.gofobao.framework.comment.vo.response.VoTopicMemberCenterResp;
import com.gofobao.framework.security.contants.SecurityContants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 用户中心
 */
@RestController
public class MemberCenterController {

    @Autowired
    TopicsUsersBiz topicsUsersBiz ;


    @GetMapping("/member-center/")
    public ResponseEntity<VoTopicMemberCenterResp> memberCenter(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicsUsersBiz.memberCenter(userId) ;
    }

}
