package com.gofobao.framework.system.controller;


import com.gofobao.framework.system.biz.ArticleBiz;
import com.gofobao.framework.system.biz.DictBiz;
import com.gofobao.framework.system.biz.FindBiz;
import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FindController {

    @Autowired
    FindBiz findBiz ;

    @Autowired
    DictBiz dictBiz ;

    @Autowired
    private ArticleBiz articleBiz;

    @GetMapping("/pub/find/index")
    public ResponseEntity<VoFindIndexResp> index() {
        return  findBiz.index() ;
    }

    @ApiOperation("发现:公告;百科;报道;发现")
    @GetMapping("/pub/find/article")
    public ResponseEntity<VoViewArticleWarpRes> articleList(VoArticleReq voArticleReq){
        return  articleBiz.list(voArticleReq);
    }


    @ApiOperation("文章详情")
    @GetMapping("/pub/find/article/info/{id}")
    public ResponseEntity<VoViewArticleInfoWarpRes> info(@PathVariable("id") Long id) {
        return articleBiz.info(id);
    }


    @ApiOperation("联系客户")
    @GetMapping("/pub/find/service")
    public ResponseEntity<VoServiceResp> service() {
        return  dictBiz.service() ;
    }

}
