package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xin on 2017/11/10.
 */
@Repository
public interface TopicReportRepository extends JpaRepository<TopicReport, Long>, JpaSpecificationExecutor<TopicReport> {

    TopicReport findTopByUserIdOrderByIdDesc(Long userId);

    List<TopicReport> findByUserIdAndSourceIdAndSourceType(Long userId, Long soucreId, Integer sourceType);
}
