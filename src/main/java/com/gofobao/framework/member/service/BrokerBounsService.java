package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.BrokerBouns;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/7/5.
 */
public interface BrokerBounsService {
    BrokerBouns save(BrokerBouns brokerBouns);
    List<BrokerBouns> save(List<BrokerBouns> brokerBounsList);
    BrokerBouns findById(Long id);
    List<BrokerBouns> findList(Specification<BrokerBouns> specification);
    List<BrokerBouns> findList(Specification<BrokerBouns> specification, Sort sort);
    List<BrokerBouns> findList(Specification<BrokerBouns> specification, Pageable pageable);
    long count(Specification<BrokerBouns> specification);
}
