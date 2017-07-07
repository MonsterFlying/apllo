package com.gofobao.framework.system.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.system.biz.ArticleBiz;
import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.VoViewArticleInfoWarpRes;
import com.gofobao.framework.system.vo.response.VoViewArticleWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/7/7.
 */
@RequestMapping("/pub/article")
@RestController
@Api(description = "文章")
public class WebArticleController {

    @Autowired
    private ArticleBiz articleBiz;


    @Autowired
    private ThymeleafHelper thymeleafHelper;

    @ApiOperation("文章列表 type: notice,help,media,news,job")
    @PostMapping("list")
    public ResponseEntity<VoViewArticleWarpRes> list(VoArticleReq articleReq) {
        return articleBiz.list(articleReq);
    }

    @ApiOperation("文章详情")
    @GetMapping("info/{id}")
    public ResponseEntity<VoViewArticleInfoWarpRes> info(@PathVariable("id") Long id) {
        return articleBiz.info(id);
    }

    @ApiOperation("关于我们")
    @GetMapping("aboutWe/{tag}")
    private ResponseEntity<VoViewArticleInfoWarpRes> aboutWe(@PathVariable("tag") String tag) {
        VoViewArticleInfoWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewArticleInfoWarpRes.class);
        String html = thymeleafHelper.build("aboutWe/pc/" + tag, null);
        warpRes.setHtml(html);
        return ResponseEntity.ok(warpRes);
    }
}
