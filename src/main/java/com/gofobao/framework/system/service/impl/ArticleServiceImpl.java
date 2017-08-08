package com.gofobao.framework.system.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.entity.Article;
import com.gofobao.framework.system.repository.ArticleRepository;
import com.gofobao.framework.system.service.ArticleService;
import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.ArticleModle;
import com.gofobao.framework.system.vo.response.FindIndexArticle;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2017/7/7.
 */
@Component
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;


    LoadingCache<String, List<FindIndexArticle>> artclesCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, List<FindIndexArticle>>() {
                @Override
                public List<FindIndexArticle> load(String type) throws Exception {
                    Specification<Article> specification = Specifications.<Article>and()
                            .eq("type", type)
                            .eq("status", 1)
                            .build();
                    List<Article> artilcList = articleRepository.findAll(specification, new Sort(new Sort.Order(Sort.Direction.DESC, "order")));
                    if (CollectionUtils.isEmpty(artilcList)) {
                        return Lists.newArrayList();
                    }
                    List<FindIndexArticle> findIndexArticles = new ArrayList<>(artilcList.size());
                    artilcList.forEach(item -> {
                        FindIndexArticle findIndexArticle = new FindIndexArticle();
                        findIndexArticles.add(findIndexArticle);
                        findIndexArticle.setId(item.getId());
                        findIndexArticle.setImageUrl(item.getPreviewImg());
                        findIndexArticle.setTime(DateHelper.dateToString(item.getCreatedAt()));
                        findIndexArticle.setTitel(item.getTitle());
                    });
                    return findIndexArticles;
                }
            });


    @Override
    public Map<String, Object> list(VoArticleReq voArticleReq) {
        Specification specification = Specifications.<Article>and()
                .eq("type", voArticleReq.getType())
                .eq("state", 1)
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
            articleModle.setTime(DateHelper.dateToString(p.getCreatedAt(), DateHelper.DATE_FORMAT_YMD));
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

    @Override
    public List<FindIndexArticle> findFindList() {
        try {
            return artclesCache.get("find");
        } catch (ExecutionException e) {
            return Lists.newArrayList();
        }
    }
}
