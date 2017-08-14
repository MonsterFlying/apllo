package com.gofobao.framework.marketing.service;

import com.gofobao.framework.marketing.entity.Marketing;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

public interface MarketingService {

    List<Marketing> findAll(Specification<Marketing> marketingSpecification);



    List<Marketing> findByDelAndOpenStateAndBeginTimeGreaterThanEqualAndEndTimeLessThanEqualAndIdIn(int del, int openState, Date startTime, Date endTime, List<Long> marketingIdList);
}
