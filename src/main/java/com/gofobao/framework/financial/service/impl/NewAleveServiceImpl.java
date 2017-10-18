package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.NewAleve;
import com.gofobao.framework.financial.repository.NewAleveRepository;
import com.gofobao.framework.financial.service.NewAleveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewAleveServiceImpl implements NewAleveService {
    @Autowired
    NewAleveRepository newAleveRepository;


    @Override
    public NewAleve findTopByQueryTimeAndTranno(String date, String tranno) {
        return newAleveRepository.findTopByQueryTimeAndTranno(date, tranno);
    }

    @Override
    public NewAleve save(NewAleve newAleve) {
        return newAleveRepository.save(newAleve);
    }

    @Override
    public Long count(Specification<NewAleve> specification) {
        return newAleveRepository.count(specification);
    }

    @Override
    public Page<NewAleve> findAll(Specification<NewAleve> specification, Pageable pageable) {
        return newAleveRepository.findAll(specification, pageable);
    }

    @Override
    public NewAleve findTopByReldateAndInptimeAndTranno(String reldate, String inptime, String tranno) {
        return newAleveRepository.findTopByReldateAndInptimeAndTranno(reldate, inptime, tranno);
    }

    @Override
    public List<NewAleve> findAll(Specification<NewAleve> specification) {
        return newAleveRepository.findAll(specification);
    }
}
