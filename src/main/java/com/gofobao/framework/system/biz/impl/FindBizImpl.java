package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.FindBiz;
import com.gofobao.framework.system.service.ArticleService;
import com.gofobao.framework.system.service.BannerService;
import com.gofobao.framework.system.service.FindService;
import com.gofobao.framework.system.vo.response.FindIndexArticle;
import com.gofobao.framework.system.vo.response.FindIndexItem;
import com.gofobao.framework.system.vo.response.IndexBanner;
import com.gofobao.framework.system.vo.response.VoFindIndexResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindBizImpl implements FindBiz {

    @Autowired
    FindService findService;

    @Autowired
    ArticleService articleService;

    @Autowired
    BannerService bannerService;


    @Override
    public ResponseEntity<VoFindIndexResp> index() {
        VoFindIndexResp voFindIndexResp = VoBaseResp.ok("查询成功", VoFindIndexResp.class);
        // 查询banner图
        List<IndexBanner> mobile = bannerService.index("mobile");
        voFindIndexResp.setBannerList(mobile);
        List<FindIndexItem>  findIndexItems = findService.findIndex();
        voFindIndexResp.setItems(findIndexItems) ;
        List<FindIndexArticle> articles = articleService.findFindList() ;
        voFindIndexResp.setArticles(articles) ;
        return ResponseEntity.ok(voFindIndexResp);
    }

    @Override
    public ResponseEntity<VoFindIndexResp> financeIndex() {
        VoFindIndexResp voFindIndexResp = VoBaseResp.ok("查询成功", VoFindIndexResp.class);
        // 查询banner图
        List<IndexBanner> mobile = bannerService.index("financer");
        voFindIndexResp.setBannerList(mobile);
        List<FindIndexItem>  findIndexItems = findService.findIndex();
        voFindIndexResp.setItems(findIndexItems) ;
        List<FindIndexArticle> articles = articleService.findFindList() ;
        voFindIndexResp.setArticles(articles) ;
        return ResponseEntity.ok(voFindIndexResp);
    }
}
