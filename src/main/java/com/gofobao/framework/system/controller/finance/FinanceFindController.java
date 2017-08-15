package com.gofobao.framework.system.controller.finance;


import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.ArticleBiz;
import com.gofobao.framework.system.biz.DictBiz;
import com.gofobao.framework.system.biz.FindBiz;
import com.gofobao.framework.system.biz.SuggestBiz;
import com.gofobao.framework.system.entity.Suggest;
import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class FinanceFindController {

    @Autowired
    FindBiz findBiz ;

    @Autowired
    DictBiz dictBiz ;

    @Autowired
    private ArticleBiz articleBiz;

    @Autowired
    private SuggestBiz suggestBiz;


    @GetMapping("/pub/finance/find/index")
    public ResponseEntity<VoFindIndexResp> index() {
        return  findBiz.index() ;
    }

    @ApiOperation("发现:公告;百科;报道;发现")
    @GetMapping("/pub/finance/find/article")
    public ResponseEntity<VoViewArticleWarpRes> articleList(VoArticleReq voArticleReq){
        return  articleBiz.list(voArticleReq);
    }


    @ApiOperation("文章详情")
    @GetMapping("/pub/finance/find/article/info/{id}")
    public ResponseEntity<VoViewArticleInfoWarpRes> info(@PathVariable("id") Long id) {
        return articleBiz.info(id);
    }


    @ApiOperation("联系我们")
    @GetMapping("/pub/finance/find/service")
    public ResponseEntity<VoServiceResp> service() {
        return  dictBiz.service() ;
    }


    @ApiOperation("反馈")
    @PostMapping("/pub/finance/find/suggest/add")
    public ResponseEntity<VoBaseResp> add(VoSuggestAddReq suggestAddReq) {
        Suggest suggest = new Suggest();
        suggest.setContent(suggestAddReq.getContent());
        suggest.setCreatedAt(new Date());
        return suggestBiz.save(suggest);
    }


}
