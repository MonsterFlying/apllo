package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.RechargeLogsBiz;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.gofobao.framework.asset.vo.response.pc.VoViewRechargeWarpRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by admin on 2017/7/3.
 */
@Service
public class RechargeLogsBizImpl implements RechargeLogsBiz {

    @Autowired
    private RechargeDetailLogService logService;

    @Override
    public ResponseEntity<VoViewRechargeWarpRes> logs(VoPcRechargeReq rechargeReq) {
        return logService.pcLogs(rechargeReq);
    }
}
