package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.repository.NewEveRepository;
import com.gofobao.framework.financial.service.NewEveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewEveServiceImpl implements NewEveService {

    @Autowired
    NewEveRepository newEveRepository;

    @Override
    public NewEve findTopByOrderno(String orderno) {
        return newEveRepository.findTopByOrderno(orderno);
    }

    @Override
    public NewEve save(NewEve newEve) {
        return newEveRepository.save(newEve);
    }

    @Override
    public long countByTranstypeAndQueryTime(String transtype, String date) {
        return newEveRepository.countByTranstypeAndQueryTime(transtype, date) ;
    }
}
