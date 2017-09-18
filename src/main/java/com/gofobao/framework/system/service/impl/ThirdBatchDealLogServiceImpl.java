package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.ThirdBatchDealLog;
import com.gofobao.framework.system.repository.ThirdBatchDealLogRepository;
import com.gofobao.framework.system.service.ThirdBatchDealLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/9/12.
 */
@Service
public class ThirdBatchDealLogServiceImpl implements ThirdBatchDealLogService {

    @Autowired
    private ThirdBatchDealLogRepository thirdBatchDealLogRepository;
    public ThirdBatchDealLog findById(long id) {
        return thirdBatchDealLogRepository.findOne(id);
    }

    public List<ThirdBatchDealLog> findList(Specification<ThirdBatchDealLog> specification) {
        return thirdBatchDealLogRepository.findAll(specification);
    }

    public List<ThirdBatchDealLog> findList(Specification<ThirdBatchDealLog> specification, Sort sort) {
        return thirdBatchDealLogRepository.findAll(specification, sort);
    }

    public List<ThirdBatchDealLog> findList(Specification<ThirdBatchDealLog> specification, Pageable pageable) {
        return thirdBatchDealLogRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<ThirdBatchDealLog> specification) {
        return thirdBatchDealLogRepository.count(specification);
    }

    public ThirdBatchDealLog save(ThirdBatchDealLog thirdBatchDealLog) {
        return thirdBatchDealLogRepository.save(thirdBatchDealLog);
    }

    public List<ThirdBatchDealLog> save(List<ThirdBatchDealLog> thirdBatchDealLogList) {
        return thirdBatchDealLogRepository.save(thirdBatchDealLogList);
    }

}
