package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.Topic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Created by xin on 2017/11/8.
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic,Long>,JpaSpecificationExecutor<Topic> {
    List<Topic> findByTopicTypeIdOrderByCreateDateDesc(long topicTypeId, Pageable pageable);

    Topic findById(long topicId);
}
