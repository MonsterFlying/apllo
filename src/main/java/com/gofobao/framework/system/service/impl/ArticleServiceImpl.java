package com.gofobao.framework.system.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.entity.Article;
import com.gofobao.framework.system.repository.ArticleRepository;
import com.gofobao.framework.system.service.ArticleService;
import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.ArticleModle;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/7/7.
 */
@Component
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public Map<String, Object> list(VoArticleReq voArticleReq) {
        Specification specification = Specifications.<Article>and()
                .eq("type", voArticleReq.getType())
                .build();
        Page<Article> articlePage = articleRepository.findAll(specification,
                new PageRequest(voArticleReq.getPageIndex(),
                        voArticleReq.getPageSize(),
                        new Sort("id")));

        Long totalCount = articlePage.getTotalElements();

        List<Article> articles = articlePage.getContent();
        List<ArticleModle> articleModles = Lists.newArrayList();
        articles.stream().forEach(p -> {
            ArticleModle articleModle = new ArticleModle();
            articleModle.setTime(DateHelper.dateToString(p.getCreatedAt(),DateHelper.DATE_FORMAT_YMD));
            articleModle.setTitle(p.getTitle());
            articleModle.setId(p.getId());
            articleModles.add(articleModle);
        });
        Map<String, Object> resultMaps = Maps.newHashMap();
        resultMaps.put("totalCount", totalCount);
        resultMaps.put("articles", articleModles);
        return resultMaps;
    }

    @Override
    public Article info(Long id) {
        try {
           return articleRepository.findOne(id);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
