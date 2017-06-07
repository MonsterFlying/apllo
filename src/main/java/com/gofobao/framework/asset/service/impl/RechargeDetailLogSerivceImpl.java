package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.repository.RechargeDetailLogRepository;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import javax.persistence.LockModeType;

/**
 * Created by Max on 17/6/7.
 */
@Service
public class RechargeDetailLogSerivceImpl implements RechargeDetailLogService{
    @Autowired
    RechargeDetailLogRepository rechargeDetailLogRepository ;

    @Override
    public void save(RechargeDetailLog rechargeDetailLog) {
        rechargeDetailLogRepository.save(rechargeDetailLog) ;
    }

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public RechargeDetailLog findTopBySeqNo(String seqNo) {
        return rechargeDetailLogRepository.findTopBySeqNoAndDel(seqNo, 0) ;
    }

    @Override
    public RechargeDetailLog findById(Long rechargeId) {
        return rechargeDetailLogRepository.findTopByIdAndDel(rechargeId, 0) ;
    }
}
