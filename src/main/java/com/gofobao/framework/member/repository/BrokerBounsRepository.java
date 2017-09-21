package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.BrokerBouns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by admin on 2017/6/7.
 */
public interface BrokerBounsRepository extends JpaRepository<BrokerBouns, Long>, JpaSpecificationExecutor<BrokerBouns> {
    /**
     * 用户总奖励
     * @param userId
     * @return
     */
    @Query("select sum(b.bounsAward) from BrokerBouns b where b.userId=:userId")
    Integer sumBounsAward( @Param("userId") Long userId);


}
