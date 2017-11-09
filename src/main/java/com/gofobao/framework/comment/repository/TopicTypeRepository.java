package com.gofobao.framework.comment.repository;

import com.gofobao.framework.comment.entity.TopicType;
import com.gofobao.framework.comment.vo.request.VoTopicTypeReq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by xin on 2017/11/8.
 */
@Repository
public interface TopicTypeRepository extends JpaRepository<TopicType,Long>,JpaSpecificationExecutor<TopicType> {
    TopicType findById(Long id);
}
