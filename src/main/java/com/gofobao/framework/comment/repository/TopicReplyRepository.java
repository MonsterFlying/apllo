package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xin on 2017/11/13.
 */
@Repository
public interface TopicReplyRepository extends JpaRepository<TopicReply, Long>, JpaSpecificationExecutor<TopicReply> {
    @Modifying
    @Query(value = "UPDATE gfb_topics_reply SET user_name = ?2 WHERE user_id = ?1", nativeQuery = true)
    Integer batchUpateUsernameByUserId(Long userId, String username);

    @Modifying
    @Query(value = "UPDATE gfb_topics_reply SET for_user_name = ?2 WHERE for_user_id = ?1", nativeQuery = true)
    Integer batchUpateUsernameByForUserId(Long userId, String username);

    @Modifying
    @Query(value = "UPDATE gfb_topics_reply SET user_icon_url = ?2 WHERE user_id = ?1", nativeQuery = true)
    Integer batchUpateAvatarByUserId(Long userId, String avatar);


    @Modifying
    @Query(value = "UPDATE gfb_topics_reply SET for_user_icon_url = ?2 WHERE for_user_id = ?1", nativeQuery = true)
    Integer batchUpateAvatarByForUserId(Long userId, String avatar);

    List<TopicReply> findByTopicCommentId(Long topicCommentId);

    @Modifying
    @Query(value = "UPDATE gfb_topics_reply SET del = 1 WHERE topic_reply_id IN ?1", nativeQuery = true)
    Integer updateReply(List<Long> id);

    @Modifying
    @Query(value = "update gfb_topics_reply set del = 1 where topic_comment_id = ?1", nativeQuery = true)
    Integer updateByComment(Long topicCommentId);
}
