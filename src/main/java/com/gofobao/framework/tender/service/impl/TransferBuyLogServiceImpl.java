package com.gofobao.framework.tender.service.impl;

import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.repository.TransferBuyLogRepository;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/7/31.
 */
@Service
public class TransferBuyLogServiceImpl implements TransferBuyLogService {

    @Autowired
    private TransferBuyLogRepository transferBuyLogRepository;

    public List<TransferBuyLog> findList(Specification<TransferBuyLog> specification) {
        return transferBuyLogRepository.findAll(specification);
    }

    public List<TransferBuyLog> findList(Specification<TransferBuyLog> specification, Sort sort) {
        return transferBuyLogRepository.findAll(specification, sort);
    }

    public List<TransferBuyLog> findList(Specification<TransferBuyLog> specification, Pageable pageable) {
        return transferBuyLogRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<TransferBuyLog> specification) {
        return transferBuyLogRepository.count(specification);
    }

    public TransferBuyLog findById(long id) {
        return transferBuyLogRepository.getOne(id);
    }


    public TransferBuyLog save(TransferBuyLog transferBuyLog){
        return transferBuyLogRepository.save(transferBuyLog);
    }

    public List<TransferBuyLog> save(List<TransferBuyLog> transferBuyLogList){
        return transferBuyLogRepository.save(transferBuyLogList);
    }
}
