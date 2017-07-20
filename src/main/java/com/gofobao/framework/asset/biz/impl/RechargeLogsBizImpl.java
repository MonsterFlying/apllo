package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.RechargeLogsBiz;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.gofobao.framework.asset.vo.response.pc.RechargeLogs;
import com.gofobao.framework.asset.vo.response.pc.VoViewRechargeWarpRes;
import com.gofobao.framework.common.jxl.ExcelException;
import com.gofobao.framework.common.jxl.ExcelUtil;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;

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

    @Override
    public void pcToExcel(VoPcRechargeReq rechargeReq, HttpServletResponse response) {
        ResponseEntity<VoViewRechargeWarpRes> rechargeDetailLogs=logService.pcLogs(rechargeReq);
        List<RechargeLogs> rechargeLogs=rechargeDetailLogs.getBody().getLogs();
        if(!CollectionUtils.isEmpty(rechargeLogs)) {
                LinkedHashMap<String, String> paramMaps = Maps.newLinkedHashMap();
                paramMaps.put("createAt", "时间");
                paramMaps.put("channel", "充值渠道");
                paramMaps.put("money", "充值金额（分）");
                paramMaps.put("status", "充值状态");
                paramMaps.put("remark", "备注");
                try {
                    ExcelUtil.listToExcel(rechargeLogs, paramMaps, "充值记录", response);
                } catch (ExcelException e) {
                    e.printStackTrace();
                }
        }
    }
}
