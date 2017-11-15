package com.gofobao.framework.comment.service;

import com.gofobao.framework.comment.entity.TopicsIntegralRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface TopicsIntegralRecordService {
    /**
     * 根据条件查找
     * @param topicsIntegralRecordSpecification
     * @param pageable
     * @return
     */
    List<TopicsIntegralRecord> findAll(Specification<TopicsIntegralRecord> topicsIntegralRecordSpecification, Pageable pageable);
}
