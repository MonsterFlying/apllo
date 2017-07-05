package com.gofobao.framework.asset.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.repository.RechargeDetailLogRepository;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.gofobao.framework.asset.vo.response.pc.RechargeLogs;
import com.gofobao.framework.asset.vo.response.pc.VoViewRechargeWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.LockModeType;
import java.util.*;

/**
 * Created by Max on 17/6/7.
 */
@Service
public class RechargeDetailLogSerivceImpl implements RechargeDetailLogService {
    @Autowired
    RechargeDetailLogRepository rechargeDetailLogRepository;

    @Override
    public void save(RechargeDetailLog rechargeDetailLog) {
        rechargeDetailLogRepository.save(rechargeDetailLog);
    }

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public RechargeDetailLog findTopBySeqNo(String seqNo) {
        return rechargeDetailLogRepository.findTopBySeqNoAndDel(seqNo, 0);
    }

    @Override
    public RechargeDetailLog findById(Long rechargeId) {
        return rechargeDetailLogRepository.findTopByIdAndDel(rechargeId, 0);
    }

    @Override
    public List<RechargeDetailLog> log(Long userId, int pageIndex, int pageSize) {
        Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
        List<RechargeDetailLog> rechargeDetailLogs = rechargeDetailLogRepository.findByUserIdAndDel(userId, 0, pageable);
        return Optional.ofNullable(rechargeDetailLogs).orElse(Collections.emptyList());
    }

    @Override
    public List<RechargeDetailLog> findByUserIdAndDelAndStateInAndCreateTimeBetween(long userId, int del, ImmutableList<Integer> stateList, Date startTime, Date endTime) {
        return rechargeDetailLogRepository.findByUserIdAndDelAndStateInAndCreateTimeBetween(userId, 0, stateList, startTime, endTime);
    }

    @Override
    public ResponseEntity<VoViewRechargeWarpRes> pcLogs(VoPcRechargeReq rechargeReq) {
        try {
            Date beginAt = DateHelper.beginOfDate(DateHelper.stringToDate(rechargeReq.getBeginAt(), DateHelper.DATE_FORMAT_YMD));
            Date endAt = DateHelper.endOfDate(DateHelper.stringToDate(rechargeReq.getEndAt(), DateHelper.DATE_FORMAT_YMD));

            Specification specification = Specifications.<RechargeDetailLog>and()
                    .eq("userId", rechargeReq.getUserId())
                    .eq("state", rechargeReq.getState())
                    .between("createTime", new Range<>(beginAt, endAt))
                    .build();
            Page<RechargeDetailLog> logPage = rechargeDetailLogRepository.findAll(specification,
                    new PageRequest(rechargeReq.getPageIndex(),
                            rechargeReq.getPageSize(),
                            new Sort(Sort.Direction.DESC, "id")));
            List<RechargeDetailLog> logList = logPage.getContent();
            VoViewRechargeWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewRechargeWarpRes.class);

            if (CollectionUtils.isEmpty(logList)) {
                warpRes.setTotalCount(0);
                warpRes.setLogs(new ArrayList<>());
                return ResponseEntity.ok(warpRes);
            }
            List<RechargeLogs> logs = new ArrayList<>(rechargeReq.getPageSize());
            logList.stream().forEach(p -> {
                RechargeLogs rechargeLogs = new RechargeLogs();
                rechargeLogs.setChannel(p.getRechargeChannel());
                rechargeLogs.setMoney(StringHelper.formatMon(p.getMoney() / 100D));
                rechargeLogs.setCreateAt(DateHelper.dateToString(p.getCreateTime()));
                rechargeLogs.setRemark(p.getRemark());
                rechargeLogs.setStatus(p.getState());
                logs.add(rechargeLogs);
            });
            Long totalCount = logPage.getTotalElements();
            warpRes.setLogs(logs);
            warpRes.setTotalCount(totalCount.intValue());
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "查询失败",
                            VoViewRechargeWarpRes.class));
        }
    }
}
