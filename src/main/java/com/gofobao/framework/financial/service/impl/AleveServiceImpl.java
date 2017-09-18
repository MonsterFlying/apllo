package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.Aleve;
import com.gofobao.framework.financial.repository.AleveRepository;
import com.gofobao.framework.financial.service.AleveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AleveServiceImpl implements AleveService {

    @Autowired
    AleveRepository aleveRepository;

    @Override
    public List<Aleve> findByTranno(String tranno) {
        return aleveRepository.findByTranno(tranno);
    }

    @Override
    public Aleve save(Aleve aleve) {
        return aleveRepository.save(aleve);
    }

    @Override
    public long count(Specification<Aleve> specification) {
        return aleveRepository.count(specification);
    }

    @Override
    public Page<Aleve> findAll(Specification<Aleve> specification, Pageable pageable) {
        return aleveRepository.findAll(specification, pageable);
    }

    @Override
    public Long countOfDateAndTranstype(String date, String type) {
        return aleveRepository.countOfDateAndTranstype(date, type) ;
    }

    @Override
    public Page<Aleve> findByDateAndTranstype(String date, String transtype, Pageable pageable) {
        return aleveRepository.findByDateAndTranstype(date, transtype, pageable) ;
    }


}
