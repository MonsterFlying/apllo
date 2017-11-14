package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xin on 2017/11/10.
 */
@Repository
public interface TopicCommentRepository extends JpaRepository<TopicComment, Long>, JpaSpecificationExecutor<TopicComment> {

    List<TopicComment> findByTopicIdAndDelOrderByIdAsc(long topicId, int i, Pageable pageable);
}
