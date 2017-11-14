package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.Topic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Created by xin on 2017/11/8.
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic,Long>,JpaSpecificationExecutor<Topic> {
    List<Topic> findByTopicTypeIdOrderByCreateDateDesc(long topicTypeId, Pageable pageable);

    Topic findById(long topicId);

    @Modifying
    @Query(value = "UPDATE gfb_topics SET user_name = ?2 WHERE user_id = ?1", nativeQuery = true)
    Integer batchUpateUsernameByUserId(Long userId, String username);

    @Modifying
    @Query(value = "UPDATE gfb_topics SET user_icon_url = ?2 WHERE user_id = ?1", nativeQuery = true)
    Integer batchUpateAvatarByUserId(Long userId, String avatar);
}
