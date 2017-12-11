package com.gofobao.framework.comment.controller;

import com.gofobao.framework.comment.biz.TopicsUsersBiz;
import com.gofobao.framework.comment.vo.request.VoUpdateUsernameReq;
import com.gofobao.framework.comment.vo.response.VoAvatarResp;
import com.gofobao.framework.comment.vo.response.VoTopicCommentManagerListResp;
import com.gofobao.framework.comment.vo.response.VoTopicListResp;
import com.gofobao.framework.comment.vo.response.VoTopicMemberCenterResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户中心
 */
@RestController
public class MemberCenterController {

    @Autowired
    TopicsUsersBiz topicsUsersBiz;

    @ApiOperation("用户中心")
    @GetMapping("/member-center/")
    public ResponseEntity<VoTopicMemberCenterResp> memberCenter(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicsUsersBiz.memberCenter(userId);
    }

    @ApiOperation("修改头像")
    @PostMapping("/member-center/avatar")
    public ResponseEntity<VoAvatarResp> avatar(HttpServletRequest httpServletRequest,
                                               @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicsUsersBiz.avatar(httpServletRequest, userId);
    }


    @ApiOperation("用户名称修改")
    @PostMapping("/member-center/username/update")
    public ResponseEntity<VoBaseResp> updateUsername(@Valid @ModelAttribute VoUpdateUsernameReq voUpdateUsernameReq,
                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicsUsersBiz.updateUsername(voUpdateUsernameReq, userId);
    }

    @ApiOperation("我的帖子")
    @GetMapping("/member-center/topic/list/{topicTypeId}/{page}")
    public ResponseEntity<VoTopicListResp> listUserTopic(@PathVariable Long topicTypeId, HttpServletRequest httpServletRequest,
                                                         @PathVariable(name = "page") Integer pageable,
                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicsUsersBiz.listUserTopic(topicTypeId, userId, pageable, httpServletRequest);
    }

    @ApiOperation("我的评论回复管理")
    @GetMapping("/member-center/comment/list/{sourceType}/{page}")
    public ResponseEntity<VoTopicCommentManagerListResp> listUserComment(@PathVariable Integer sourceType, HttpServletRequest httpServletRequest,
                                                                         @PathVariable(name = "page") Integer pageIndex,
                                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicsUsersBiz.listComment(sourceType, httpServletRequest, pageIndex, userId);
    }

    @ApiOperation("@我的评论回复管理")
    @GetMapping("/member-center/bycomment/list/{sourceType}/{page}")
    public ResponseEntity<VoTopicCommentManagerListResp> listUserByComment(@PathVariable Integer sourceType, HttpServletRequest httpServletRequest,
                                                                           @PathVariable(name = "page") Integer pageable,
                                                                           @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return topicsUsersBiz.listByComment(sourceType, httpServletRequest, pageable, userId);
    }
}
