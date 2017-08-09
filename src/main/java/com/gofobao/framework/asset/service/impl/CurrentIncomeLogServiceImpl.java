package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.CurrentIncomeLog;
import com.gofobao.framework.asset.repository.CurrentIncomeLogRepository;
import com.gofobao.framework.asset.service.CurrentIncomeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrentIncomeLogServiceImpl implements CurrentIncomeLogService {

    @Autowired
    CurrentIncomeLogRepository currentIncomeLogRepository;

    @Override
    public List<CurrentIncomeLog> findBySeqNoAndState(String no, int state) {
        return currentIncomeLogRepository.findBySeqNoAndState(no, state);
    }

    @Override
    public CurrentIncomeLog save(CurrentIncomeLog currentIncomeLog) {
        return currentIncomeLogRepository.save(currentIncomeLog);
    }
}
