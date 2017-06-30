package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.repository.ThirdBatchLogRepository;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/6/15.
 */
@Service
public class ThirdBatchLogServiceImpl implements ThirdBatchLogService {

    @Autowired
    private ThirdBatchLogRepository thirdBatchLogRepository;

    public List<ThirdBatchLog> findList(Specification<ThirdBatchLog> specification) {
        return thirdBatchLogRepository.findAll(specification);
    }

    public List<ThirdBatchLog> findList(Specification<ThirdBatchLog> specification, Sort sort) {
        return thirdBatchLogRepository.findAll(specification, sort);
    }

    public List<ThirdBatchLog> findList(Specification<ThirdBatchLog> specification, Pageable pageable) {
        return thirdBatchLogRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<ThirdBatchLog> specification) {
        return thirdBatchLogRepository.count(specification);
    }

    public ThirdBatchLog save(ThirdBatchLog thirdBatchLog) {
        return thirdBatchLogRepository.save(thirdBatchLog);
    }

    public List<ThirdBatchLog> save(List<ThirdBatchLog> thirdBatchLogList) {
        return thirdBatchLogRepository.save(thirdBatchLogList);
    }
}
