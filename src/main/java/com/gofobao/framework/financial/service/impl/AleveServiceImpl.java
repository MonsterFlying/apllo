package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.Aleve;
import com.gofobao.framework.financial.repository.AleveRepository;
import com.gofobao.framework.financial.service.AleveService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
