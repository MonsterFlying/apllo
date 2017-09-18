package com.gofobao.framework.asset.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.repository.CashDetailLogRepository;
import com.gofobao.framework.asset.service.CashDetailLogService;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.gofobao.framework.asset.vo.response.pc.VoCashLog;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.system.repository.DictValueRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private DictValueRepository dictValueRepository;

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
        return Optional.ofNullable(cashDetailLogRepository.findByUserIdAndStateInAndCreateTimeBetween(userId, stateList, startDate, endDate)).orElse(Collections.EMPTY_LIST) ;
    }

    /**
     * 提现记录
     * @param voPcCashLogs
     * @return
     */
    @Override
    public List<VoCashLog> pcLogs(VoPcCashLogs voPcCashLogs) {
        Specification specification = Specifications.<CashDetailLog>and()
                .eq("userId", voPcCashLogs.getUserId())
                .eq(!StringUtils.isEmpty(voPcCashLogs.getStatus()),"state", voPcCashLogs.getStatus())
                .build();
        Page<CashDetailLog> cashDetailLogPage = cashDetailLogRepository.findAll(specification,
                new PageRequest(voPcCashLogs.getPageIndex(),
                        voPcCashLogs.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id")));

        List<CashDetailLog> cashDetailLogs = cashDetailLogPage.getContent();
        if (CollectionUtils.isEmpty(cashDetailLogs)) {
            return Collections.EMPTY_LIST;
        }
        Long totalCount = cashDetailLogPage.getTotalElements();

        final int[] num = {0};
        List<VoCashLog> logs = Lists.newArrayList();
        cashDetailLogs.stream().forEach(p -> {
            VoCashLog cashLog = new VoCashLog();
            cashLog.setId(p.getId());
            cashLog.setBankNo(UserHelper.hideChar(p.getCardNo(), UserHelper.BANK_ACCOUNT_NUM));
            cashLog.setBanKName(p.getBankName());
            cashLog.setCreateTime(DateHelper.dateToString(p.getCreateTime()));
            cashLog.setMoney(StringHelper.toString(p.getMoney() / 100D));
            cashLog.setServiceCharge(StringHelper.toString(p.getFee() / 100D));
            if (num[0] == 0) {
                cashLog.setTotalCount(totalCount.intValue());
                num[0] = 1;
            }
            logs.add(cashLog);
        });

        return logs;
    }


}
