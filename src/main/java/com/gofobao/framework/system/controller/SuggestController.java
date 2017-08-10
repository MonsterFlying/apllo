package com.gofobao.framework.system.controller;

import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.system.biz.SuggestBiz;
import com.gofobao.framework.system.entity.Suggest;
import com.gofobao.framework.system.vo.response.VoSuggestAddReq;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;

/**
 * Created by admin on 2017/8/10.
 */

@Api(description = "站内信")
@RequestMapping("/suggest/v2")
@RestController
public class SuggestController {

    @Autowired
    private SuggestBiz suggestBiz;


    @RequestMapping("/add")
    public ResponseEntity<Boolean> add(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, VoSuggestAddReq suggestAddReq) {
        Suggest suggest = new Suggest();
        suggest.setContent(suggestAddReq.getContent());
        suggest.setCreatedAt(new Date());
        suggest.setUserId(userId);
        return suggestBiz.save(suggest);
    }


}
