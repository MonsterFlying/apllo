package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.Article;
import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.FindIndexArticle;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/7/7.
 */
public interface ArticleService {


    /**
     * 文章列表
     * @param voArticleReq
     * @return
     */
    Map<String,Object> list(VoArticleReq voArticleReq);

    /**
     * 文章详情
     * @param id
     * @return
     */
    Article info(Long id);


    /**
     *  发现栏目
     * @return
     */
    List<FindIndexArticle> findFindList();

}
