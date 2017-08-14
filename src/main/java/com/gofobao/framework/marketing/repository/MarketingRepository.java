package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.Marketing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MarketingRepository extends JpaRepository<Marketing, Long>, JpaSpecificationExecutor<Marketing>{
    List<Marketing> findByDelAndOpenStateAndBeginTimeGreaterThanEqualAndEndTimeLessThanEqualAndIdIn(int del, int openState, Date startTime, Date endTime, List<Long> marketingIdList);

}
