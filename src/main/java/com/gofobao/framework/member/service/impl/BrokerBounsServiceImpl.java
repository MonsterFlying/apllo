package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.BrokerBouns;
import com.gofobao.framework.member.repository.BrokerBounsRepository;
import com.gofobao.framework.member.service.BrokerBounsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/7/5.
 */
@Service
public class BrokerBounsServiceImpl implements BrokerBounsService {

    @Autowired
    private BrokerBounsRepository brokerBounsRepository;

    public BrokerBouns save(BrokerBouns brokerBouns) {
        return brokerBounsRepository.save(brokerBouns);
    }

    public List<BrokerBouns> save(List<BrokerBouns> brokerBounsList) {
        return brokerBounsRepository.save(brokerBounsList);
    }

    public BrokerBouns findById(Long id) {
        return brokerBounsRepository.findOne(id);
    }

    public List<BrokerBouns> findList(Specification<BrokerBouns> specification) {
        return brokerBounsRepository.findAll(specification);
    }

    public List<BrokerBouns> findList(Specification<BrokerBouns> specification, Sort sort) {
        return brokerBounsRepository.findAll(specification, sort);
    }

    public List<BrokerBouns> findList(Specification<BrokerBouns> specification, Pageable pageable) {
        return brokerBounsRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<BrokerBouns> specification) {
        return brokerBounsRepository.count(specification);
    }
}
