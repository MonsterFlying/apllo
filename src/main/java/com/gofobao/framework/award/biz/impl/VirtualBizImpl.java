package com.gofobao.framework.award.biz.impl;

import com.gofobao.framework.award.biz.VirtualBiz;
import com.gofobao.framework.award.service.VirtualService;
import com.gofobao.framework.award.vo.response.VirtualStatistics;
import com.gofobao.framework.award.vo.response.VoViewVirtualStatisticsWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by admin on 2017/6/8.
 */
@Service
public class VirtualBizImpl implements VirtualBiz {

    @Autowired
    private VirtualService virtualService;

    @Override
    public ResponseEntity<VoViewVirtualStatisticsWarpRes> query(Long userId) {
        try {
            VirtualStatistics virtualStatistics = virtualService.statistics(userId);
            VoViewVirtualStatisticsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewVirtualStatisticsWarpRes.class);
            warpRes.setVirtualStatistics(virtualStatistics);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewVirtualStatisticsWarpRes.class));
        }
    }
}
