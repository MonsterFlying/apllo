package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.SysVersionBiz;
import com.gofobao.framework.system.contants.VersionContants;
import com.gofobao.framework.system.entity.SysVersion;
import com.gofobao.framework.system.service.SysVersionService;
import com.gofobao.framework.system.vo.response.VoSysVersion;
import com.gofobao.framework.system.vo.response.VoViewSysVersionWarpRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by admin on 2017/6/21.
 */
@Service
public class SysVserionBizImpl implements SysVersionBiz {
    @Autowired
    private SysVersionService sysVersionService;

    @Override
    public ResponseEntity<VoBaseResp> list(Integer terminal, Integer clientId) {
        List<SysVersion> sysVersions = sysVersionService.list(terminal);
        if (CollectionUtils.isEmpty(sysVersions)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求"));
        }
        SysVersion sysVersion = sysVersions.get(0);
        boolean flag = sysVersion.getVersionId() > clientId;
        VoSysVersion voSysVersion = new VoSysVersion();
        if (flag) {  // 需要
            voSysVersion.setIsEquls(VersionContants.EQULSNO);
            voSysVersion.setIsNew(true);
            voSysVersion.setViewVersion(sysVersion.getViewVersion());
            voSysVersion.setDetails(sysVersion.getDetails());
            voSysVersion.setForce(sysVersion.getForce());
            voSysVersion.setUrl(sysVersion.getRul());
        } else {   // 不需要
            voSysVersion.setIsEquls(VersionContants.EQULSOK);
            voSysVersion.setViewVersion(sysVersion.getViewVersion());
            voSysVersion.setDetails(sysVersion.getDetails());
            voSysVersion.setForce(sysVersion.getForce());
            voSysVersion.setIsNew(false);
        }
        VoViewSysVersionWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewSysVersionWarpRes.class);
        warpRes.setVoSysVersion(voSysVersion);
        return ResponseEntity.ok(warpRes);
    }
}
