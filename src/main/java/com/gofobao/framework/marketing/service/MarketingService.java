package com.gofobao.framework.marketing.service;

import com.gofobao.framework.marketing.entity.Marketing;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface MarketingService {

    List<Marketing> findAll(Specification<Marketing> marketingSpecification);

    List<Marketing> findByDelAndOpenStateAndIdIn(int del, int openState, List<Long> marketingIdList);

}
