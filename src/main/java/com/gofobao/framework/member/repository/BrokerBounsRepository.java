package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.BrokerBouns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by admin on 2017/6/7.
 */
public interface BrokerBounsRepository extends JpaRepository<BrokerBouns,Long>,JpaSpecificationExecutor<BrokerBouns> {
}
