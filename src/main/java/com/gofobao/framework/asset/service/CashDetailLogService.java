package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.CashDetailLog;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
public interface CashDetailLogService {
    List<CashDetailLog> findByStateInAndUserId(ImmutableList<Integer> states, long userId);

    void save(CashDetailLog cashDetailLog);

    /**
     * 根据流水号查询充值记录
     * @param seqNo
     * @return
     */
    CashDetailLog findTopBySeqNoLock(String seqNo);

    /**
     * 获取提现记录
     * @param userId
     * @param page
     * @return
     */
    List<CashDetailLog> findByUserIdAndPage(Long userId, PageRequest page);

    CashDetailLog findById(Long id) ;
}
