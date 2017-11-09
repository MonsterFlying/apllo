package com.gofobao.framework.comment.controller;

import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.comment.service.TopicTypeService;
import com.gofobao.framework.comment.vo.response.VoTopicResp;
import com.gofobao.framework.comment.vo.response.VoTopicTypeListResp;
import com.gofobao.framework.comment.vo.response.VoTopicTypeResp;
import com.gofobao.framework.security.contants.SecurityContants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * Created by xin on 2017/11/8.
 */
@RestController
public class TopicTypeController {
    @Autowired
    private TopicTypeService topicTypeService;


    @PostMapping("/comment/topicType/publish")
    public ResponseEntity<VoTopicTypeResp> publishTopicType(VoDoAgainVerifyReq voDoAgainVerifyReq){
        return topicTypeService.publishTopicType(voDoAgainVerifyReq);
    }


    @GetMapping("/comment/topicType/del/{id}")
    public ResponseEntity<VoTopicTypeResp> delTopicType(@PathVariable Integer id,@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return topicTypeService.delTopicType(id,userId);
    }

    @GetMapping("/pub/comment/topicType/list")
    public ResponseEntity<VoTopicTypeListResp> listTopicType(){
        return topicTypeService.listTopicType();
    }

}
