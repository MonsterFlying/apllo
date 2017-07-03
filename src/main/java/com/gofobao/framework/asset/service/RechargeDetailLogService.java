package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.List;

/**
 * Created by Max on 17/6/7.
 */
public interface RechargeDetailLogService {

    void save(RechargeDetailLog rechargeDetailLog);

    RechargeDetailLog findTopBySeqNo(String seqNo);

    RechargeDetailLog findById(Long rechargeId);

    List<RechargeDetailLog> log(Long userId, int pageIndex, int pageSize);

    List<RechargeDetailLog> findByUserIdAndDelAndStateInAndCreateTimeBetween(long userId, int del, ImmutableList<Integer> stateList, Date startTime, Date startTime1);

    List<RechargeDetailLog>pcLogs(VoPcRechargeReq rechargeReq);
}
