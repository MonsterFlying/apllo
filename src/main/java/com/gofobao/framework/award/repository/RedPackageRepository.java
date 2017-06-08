package com.gofobao.framework.award.repository;

import com.gofobao.framework.award.entity.ActivityRedPacket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
public interface RedPackageRepository extends JpaRepository<ActivityRedPacket,Long>,JpaSpecificationExecutor<ActivityRedPacket> {
    /**
     * 未领取
     * @param userId
     * @param status
     * @param date
     * @return
     */
    Page<ActivityRedPacket> findByUserIdAndStatusIsAndEndAtGreaterThanEqual(Long userId, Integer status, Date date,Pageable pageable);

    /**
     * 以领取
     * @param userId
     * @param status
     * @return
     */
    Page<ActivityRedPacket>findByUserIdAndStatusIs(Long userId,Integer status,Pageable pageable);

    /**
     * 已过期
     * @param userId
     * @param status
     * @param date
     * @return
     */
    Page<ActivityRedPacket> findByUserIdAndStatusIsOrEndAtLessThanEqual(Long userId, Integer status, Date date, Pageable pageable);

}
