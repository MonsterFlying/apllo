package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.system.biz.ArticleBiz;
import com.gofobao.framework.system.service.ArticleService;
import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.ArticleModle;
import com.gofobao.framework.system.vo.response.VoViewArticleInfoWarpRes;
import com.gofobao.framework.system.vo.response.VoViewArticleWarpRes;
import freemarker.template.utility.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/7/7.
 */
@Service
public class ArticleBizImpl implements ArticleBiz {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private ThymeleafHelper thymeleafHelper;

    @Override
    public ResponseEntity<VoViewArticleWarpRes> list(VoArticleReq voArticleReq) {
        try {
            Map<String, Object> resultMaps = articleService.list(voArticleReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<ArticleModle> articles = (List<ArticleModle>) resultMaps.get("articles");
            VoViewArticleWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewArticleWarpRes.class);
            warpRes.setTotalCount(totalCount);
            warpRes.setArticleModles(articles);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "查询失败",
                            VoViewArticleWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewArticleInfoWarpRes> info(Long id) {
        VoViewArticleInfoWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewArticleInfoWarpRes.class);
        String content = thymeleafHelper.build("load_error", null);
        try {
            String articleHtml = articleService.info(id);
            if (articleHtml!=null) {
                warpRes.setHtml(articleHtml);
            } else {
                warpRes.setHtml(content);
            }
        } catch (Exception e) {
            warpRes.setHtml(content);
        }
        return ResponseEntity.ok(warpRes);
    }
}


