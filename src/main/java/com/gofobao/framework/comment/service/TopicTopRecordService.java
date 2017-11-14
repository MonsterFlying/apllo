package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.TopicTopRecord;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * 点赞服务类
 */
public interface TopicTopRecordService {

    /**
     * 根据多个维度查询点赞记录
     *
     * @param userId
     * @param soucreId
     * @param sourceType
     * @return
     */
    List<TopicTopRecord> findByUserIdAndSourceIdAndSourceType(Long userId, Long soucreId, Integer sourceType);

    /**
     * 删除用户点赞记录
     *
     * @param topicTopRecordList
     * @return
     */
    void delete(List<TopicTopRecord> topicTopRecordList);

    /**
     * 保存点赞记录
     *
     * @param topicTopRecord
     * @return
     */
    TopicTopRecord save(TopicTopRecord topicTopRecord);

    /**
     * 根据条件查询
     * @param specification
     * @return
     */
    List<TopicTopRecord> find(Specification<TopicTopRecord> specification);

}
