package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicsNotices;
import com.gofobao.framework.comment.vo.response.VoTopicCommentManagerResp;
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
public interface TopicsNoticesRepository extends JpaRepository<TopicsNotices, Long>, JpaSpecificationExecutor<TopicsNotices> {

    @Modifying
    @Query(value = "UPDATE gfb_topics_notices SET for_user_name = ?2 WHERE for_user_id = ?1", nativeQuery = true)
    Integer batchUpateUsernameByForUserId(Long userId, String username);


    @Modifying
    @Query(value = "UPDATE gfb_topics_notices SET for_user_icon_url = ?2 WHERE for_user_id = ?1", nativeQuery = true)
    Integer batchUpateAvatarByForUserId(Long userId, String avatar);

    List<TopicsNotices> findByUserIdAndSourceType(Long userId, Integer sourceType);

    List<TopicsNotices> findByForUserIdAndSourceType(Long userId, Integer sourceType);
}
