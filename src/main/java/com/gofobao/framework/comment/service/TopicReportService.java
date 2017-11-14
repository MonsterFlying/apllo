package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.TopicReport;

import java.util.List;

public interface TopicReportService {
    /**
     * 根据举报用户ID 查询最新一条记录
     * @param userId
     * @return
     */
    TopicReport findTopByUserIdOrderByIdDesc(Long userId);


    /**
     * 查询用户举报内容
     * @param userId
     * @param soucreId
     * @param sourceType
     * @return
     */
    List<TopicReport> findByUserIdAndSourceIdAndSourceType(Long userId, Long soucreId, Integer sourceType);

    TopicReport save(TopicReport topicReport);
}
