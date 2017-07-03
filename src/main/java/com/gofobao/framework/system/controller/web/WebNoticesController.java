package com.gofobao.framework.system.controller.web;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.system.biz.NoticesBiz;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.response.VoViewUserNoticesWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by admin on 2017/6/28.
 */
@Api(description = "pc:站内信")
@RestController
@RequestMapping("notices/pc/v2")
public class WebNoticesController {

    @Autowired
    private NoticesBiz noticesBiz;

    @ApiOperation("站内信列表")
    @RequestMapping("/list/{pageIndex}/{pageSize}")
    public List<VoViewUserNoticesWarpRes> list(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                               @PathVariable("pageIndex") Integer pageIndex,
                                               @PathVariable("pageSize")Integer pageSize) {
        VoNoticesReq voNoticesReq = new VoNoticesReq();
        voNoticesReq.setUserId(userId);
        voNoticesReq.setPageIndex(pageIndex);
        voNoticesReq.setPageSize(pageSize);
        noticesBiz.list(voNoticesReq);
        return null;
    }

}
