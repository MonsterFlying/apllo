package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.vo.response.VoTopicResp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public interface TopicRepository extends JpaRepository<Topic, Long>, JpaSpecificationExecutor<Topic> {

    Topic findById(long topicId);

    @Modifying
    @Query(value = "UPDATE gfb_topics SET user_name = ?2 WHERE user_id = ?1", nativeQuery = true)
    Integer batchUpateUsernameByUserId(Long userId, String username);

    @Modifying
    @Query(value = "UPDATE gfb_topics SET user_icon_url = ?2 WHERE user_id = ?1", nativeQuery = true)
    Integer batchUpateAvatarByUserId(Long userId, String avatar);

    @Modifying
    @Query(value = "update gfb_topics set content_total_num = content_total_num+1 where id=?1", nativeQuery = true)
    void updateToTalComment(long topicId);

    Topic findByIdAndDel(long topicId, int i);

    @Modifying
    @Query(value = "update gfb_topics set del=1 where id = ?1", nativeQuery = true)
    int updateDel(long id);

    List<Topic> findByTopicTypeIdAndDelAndUserIdOrderByCreateDateDesc(Long topicTypeId, int i, Long userId,Pageable pageable);

    Topic findTopByUserIdOrderByIdDesc(Long userId);

    @Modifying
    @Query(value = "update gfb_topics set content_total_num = content_total_num-1 where id=?1", nativeQuery = true)
    Integer delToTalComment(Long topicId);

    List<Topic> findByUserIdAndDel(Long userId, int i);
}
