package com.gofobao.framework.system.controller.web;

import com.gofobao.framework.system.vo.response.VoViewUserNoticesWarpRes;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by admin on 2017/6/28.
 */
@Api(description = "pc:站内信")
@RestController
@RequestMapping("notices")
public class WebNoticesController {


    public List<VoViewUserNoticesWarpRes> noticesWarpRes(){return null;}

}
