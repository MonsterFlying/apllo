package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.CurrentIncomeLog;

import java.util.List;

public interface CurrentIncomeLogService {
    List<CurrentIncomeLog> findBySeqNoAndState(String no, int state);

    CurrentIncomeLog save(CurrentIncomeLog currentIncomeLog);
}
