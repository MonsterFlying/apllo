package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.TopicsNotices;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopicsNoticesService {

    /**
     * 条件查询
     * @param topicsReplyNoticesSpecification
     * @return
     */
    List<TopicsNotices> findAll(Specification<TopicsNotices> topicsReplyNoticesSpecification);


    /**
     * 条件查询总数
     * @param topicsReplyNoticesSpecification
     * @return
     */
    Long count(Specification<TopicsNotices> topicsReplyNoticesSpecification);

    /**
     *  批量修改冗余数据
     * @param userId
     * @param username
     * @param avatar
     */
    void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception;

    /**
     * 保存
     * @param notices
     * @return
     */
    TopicsNotices save(TopicsNotices notices);

}
