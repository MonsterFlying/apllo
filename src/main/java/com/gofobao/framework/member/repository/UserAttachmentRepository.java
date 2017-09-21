package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.UserAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/6/10.
 */
@Repository
public interface UserAttachmentRepository extends JpaRepository<UserAttachment,Long>,JpaSpecificationExecutor<UserAttachment>{
        /**
         * 用户附件
         * @param userId
         * @return
         */
        List<UserAttachment>findByUserId(Long userId);

}

