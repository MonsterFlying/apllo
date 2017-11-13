package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicType;
import com.gofobao.framework.comment.vo.request.VoTopicTypeReq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by xin on 2017/11/8.
 */
@Repository
public interface TopicTypeRepository extends JpaRepository<TopicType,Long>,JpaSpecificationExecutor<TopicType> {
    TopicType findById(Long id);

    @Modifying
    @Query(value = "update gfb_topic_type set topic_total_num = topic_total_num + 1 , update_date = ?2 where id = ?1 ",
    nativeQuery = true)
    int updateTopicTotalNum(long topicTypeId, Date nowDate);
}
