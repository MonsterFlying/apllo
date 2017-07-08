package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by admin on 2017/7/7.
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article,Long>,JpaSpecificationExecutor<Article> {
}
