package com.gofobao.framework.asset.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.repository.CashDetailLogRepository;
import com.gofobao.framework.asset.service.CashDetailLogService;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Service
public class CashDetailLogServiceImpl implements CashDetailLogService {

    @Autowired
    CashDetailLogRepository cashDetailLogRepository;

    @Override
    public List<CashDetailLog> findByStateInAndUserId(ImmutableList<Integer> states, long userId) {
        return cashDetailLogRepository.findByStateInAndUserId(states, userId);
    }

    @Override
    public void save(CashDetailLog cashDetailLog) {
        cashDetailLogRepository.save(cashDetailLog);
    }

    @Override
    public CashDetailLog findTopBySeqNoLock(String seqNo) {
        return cashDetailLogRepository.findTopBySeqNo(seqNo);
    }

    @Override
    public List<CashDetailLog> findByUserIdAndPage(Long userId, Pageable page) {
        List<CashDetailLog> byUserIdAndPage = cashDetailLogRepository.findByUserId(userId, page);
        Optional<List<CashDetailLog>> optional = Optional.ofNullable(byUserIdAndPage);
        return optional.orElse(Collections.EMPTY_LIST);
    }

    @Override
    public CashDetailLog findById(Long id) {
        return cashDetailLogRepository.findOne(id);
    }

    @Override
    public List<CashDetailLog> findByUserIdAndStateInAndCreateTimeBetween(Long userId, ImmutableList<Integer> stateList, Date startDate, Date endDate) {
        return cashDetailLogRepository.findByUserIdAndStateInAndCreateTimeBetween(userId, stateList, startDate, endDate);
    }


    @Override
    public List<CashDetailLog> pcLogs(VoPcCashLogs voPcCashLogs) {
        Specification specification = Specifications.<CashDetailLog>and()
                .eq("userId", voPcCashLogs.getUserId())
                .eq("status", voPcCashLogs.getStatus())
                .build();
        Page<CashDetailLog> cashDetailLogPage=cashDetailLogRepository.findAll(specification,
                new PageRequest(voPcCashLogs.getPageIndex(),
                        voPcCashLogs.getPageSize(),
                        new Sort(Sort.Direction.DESC,"id")));

        cashDetailLogPage.getTotalElements();

        return null;
    }
}
