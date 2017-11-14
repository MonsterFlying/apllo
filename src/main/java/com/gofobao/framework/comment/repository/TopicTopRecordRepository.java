package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicTopRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xin on 2017/11/10.
 */
@Repository
public interface TopicTopRecordRepository extends JpaRepository<TopicTopRecord, Long>, JpaSpecificationExecutor<TopicTopRecord> {

    List<TopicTopRecord> findByUserIdAndSourceIdAndSourceType(Long userId, Long soucreId, Integer sourceType);
}
