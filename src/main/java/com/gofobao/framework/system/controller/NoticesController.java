package com.gofobao.framework.system.controller;

import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.system.biz.NoticesBiz;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.response.VoViewNoticesInfoWarpRes;
import com.gofobao.framework.system.vo.response.VoViewUserNoticesWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/6/15.
 */
@Api(description = "站内信")
@RequestMapping("/notices")
@RestController
public class NoticesController {
    @Autowired
    private NoticesBiz noticesBiz;



    @ApiOperation(value="站内信列表")
    @RequestMapping(path = "v2/list/{pageIndex}/{pageSize}", method = RequestMethod.GET)
    public ResponseEntity<VoViewUserNoticesWarpRes> list(@PathVariable Integer pageIndex,
                                                         @PathVariable Integer pageSize,
                                                         @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoNoticesReq voNoticesReq = new VoNoticesReq();
        voNoticesReq.setUserId(userId);
        voNoticesReq.setPageIndex(pageIndex);
        voNoticesReq.setPageSize(pageSize);
        return noticesBiz.list(voNoticesReq);
    }


    @ApiOperation("站内信内容")
    @RequestMapping(path = "v2/info/{noticesId}", method = RequestMethod.GET)
    public ResponseEntity<VoViewNoticesInfoWarpRes> info(@PathVariable Long noticesId,
                                                         @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoNoticesReq voNoticesReq = new VoNoticesReq();
        voNoticesReq.setType(0);
        voNoticesReq.setUserId(userId);
        voNoticesReq.setId(noticesId);
        return noticesBiz.info(voNoticesReq);
    }

}
