package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.RechargeDetailLog;

/**
 * Created by Max on 17/6/7.
 */
public interface RechargeDetailLogService {

    void save(RechargeDetailLog rechargeDetailLog);

    RechargeDetailLog findTopBySeqNo(String seqNo);

    RechargeDetailLog findById(Long rechargeId);
    
}