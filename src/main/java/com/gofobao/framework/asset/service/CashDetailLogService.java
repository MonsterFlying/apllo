package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.gofobao.framework.asset.vo.response.pc.VoCashLog;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
public interface CashDetailLogService {

    List<CashDetailLog> findByStateInAndUserId(ImmutableList<Integer> states, long userId);

    /**
     * 保存
     * @param cashDetailLog
     * @return
     */
    CashDetailLog save(CashDetailLog cashDetailLog);

    /**
     * 根据条件查询提现记录
     * @param cashDetailLogSpecification
     * @return
     */

    List<CashDetailLog> findAll(Specification<CashDetailLog> cashDetailLogSpecification) ;

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
    List<CashDetailLog> findByUserIdAndPage(Long userId, Pageable page);

    CashDetailLog findById(Long id) ;

    List<CashDetailLog> findByUserIdAndStateInAndCreateTimeBetween(Long userId, ImmutableList<Integer> stateList, Date startDate, Date endDate);

    /**
     *
     * @param voPcCashLogs
     * @return
     */
    List<VoCashLog> pcLogs(VoPcCashLogs voPcCashLogs);

    long count(Specification<CashDetailLog> cashDetailLogSpecification);
}
