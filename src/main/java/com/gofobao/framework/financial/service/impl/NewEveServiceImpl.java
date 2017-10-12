package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.repository.NewEveRepository;
import com.gofobao.framework.financial.service.NewEveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return newEveRepository.countByTranstypeAndQueryTime(transtype, date);
    }

    @Override
    public NewEve findTopByOrdernoAndQueryTime(String orderno, String date) {
        return newEveRepository.findTopByOrdernoAndQueryTime(orderno, date);
    }

    @Override
    public NewEve findTopByCendtAndTranno(String cendt, String tranno) {
        return newEveRepository.findTopByCendtAndTranno(cendt, tranno);
    }

    @Override
    public List<NewEve> findByTranstypeAndQueryTime(String transtype, String date, Pageable pageable) {
        return newEveRepository.findByTranstypeAndQueryTime(transtype, date, pageable);
    }

    @Override
    public List<NewEve> findAll(Specification<NewEve> specification) {
        return newEveRepository.findAll(specification);
    }

    @Override
    public Page<Object[]> findLocalAssetChangeRecord(String beginDate, String endDate, Pageable pageable) {
        return newEveRepository.findByCreateTime(beginDate, endDate, pageable);
    }

    @Override
    public Page<Object[]> findRemoteByQueryTime(String date, Pageable pageable) {
        return newEveRepository.findRemoteByQueryTime(date, pageable);
    }
}
