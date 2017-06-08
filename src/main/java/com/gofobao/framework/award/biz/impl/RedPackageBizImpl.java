package com.gofobao.framework.award.biz.impl;

import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.award.service.RedPackageService;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.RedPackageRes;
import com.gofobao.framework.award.vo.response.VoViewRedPackageWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Service
public class RedPackageBizImpl implements RedPackageBiz {

    @Autowired
    private RedPackageService redPackageService;

    @Override
    public ResponseEntity<VoViewRedPackageWarpRes> list(VoRedPackageReq voRedPackageReq) {
        try {
            List<RedPackageRes> redPackageRes = redPackageService.list(voRedPackageReq);
            VoViewRedPackageWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewRedPackageWarpRes.class);
            warpRes.setResList(redPackageRes);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewRedPackageWarpRes.class));
        }
    }
}
