package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicsIntegralRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by xin on 2017/11/10.
 */
@Repository
public interface TopicsIntegralRecordRepository extends JpaRepository<TopicsIntegralRecord,Long>,JpaSpecificationExecutor<TopicsIntegralRecord> {

}
