package com.gofobao.framework.tender.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.vo.response.UserAutoTender;
import com.gofobao.framework.tender.vo.response.VoViewUserAutoTenderWarpRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/5/27.
 */
@Service
public class AutoTenderBizImpl implements AutoTenderBiz {
    @Autowired
    private AutoTenderService autoTenderService;

    @Override
    public ResponseEntity<VoViewUserAutoTenderWarpRes> list(Long userId) {
        try {
            VoViewUserAutoTenderWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewUserAutoTenderWarpRes.class);
            List<UserAutoTender> autoTenderList = autoTenderService.list(userId);
            warpRes.setTenderList(autoTenderList);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "", VoViewUserAutoTenderWarpRes.class));
        }

    }
}
