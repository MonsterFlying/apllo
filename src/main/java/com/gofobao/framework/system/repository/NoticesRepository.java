package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.Notices;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by Max on 17/6/5.
 */
@Repository
public interface NoticesRepository extends JpaRepository<Notices, Long>,JpaSpecificationExecutor<Notices>{

    /**
     * 批量删除
     * @param date
     * @param userId
     * @param id
     * @return
     */
    @Transactional
    @Modifying
    @Query("update Notices n set n.deletedAt =?1  WHERE  n.userId=?2 AND  n.id IN ?3")
    int delete(Date date,Long userId, List<Long> id);

    /**
     *批量已读
     * @param userId
     * @param id
     * @return
     */
    @Transactional
    @Modifying
    @Query("update Notices n set n.read =1  WHERE  n.userId=?1 AND  n.id IN ?2")
    int update(Long userId, List<Long> id);


}
