package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xin on 2017/11/10.
 */
@Repository
public interface TopicCommentRepository extends JpaRepository<TopicComment, Long>, JpaSpecificationExecutor<TopicComment> {

    List<TopicComment> findByTopicIdOrderByIdAsc(long topicId, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE gfb_topics_comment SET user_name = ?2 WHERE user_id = ?1", nativeQuery = true)
    Integer batchUpateUsernameByUserId(Long userId, String username);

    @Modifying
    @Query(value = "UPDATE gfb_topics_comment SET user_icon_url = ?2 WHERE user_id = ?1", nativeQuery = true)
    Integer batchUpateAvatarByUserId(Long userId, String avatar);

    List<TopicComment> findByTopicIdAndDelOrderByIdAsc(long topicId, int del, Pageable pageable);

    @Modifying
    @Query(value = "update gfb_topics_comment set del = 1 where topic_id = ?1",nativeQuery = true)
    Integer updateComment(long id);

    TopicComment findTopByUserIdOrderByIdDesc(Long userId);
}
