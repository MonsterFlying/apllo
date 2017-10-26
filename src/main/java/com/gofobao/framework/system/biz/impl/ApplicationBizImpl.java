package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.ApplicationBiz;
import com.gofobao.framework.system.entity.Application;
import com.gofobao.framework.system.service.ApplicationService;
import com.gofobao.framework.system.vo.response.ApplicationWarpRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
@Service
public class ApplicationBizImpl implements ApplicationBiz {
    @Autowired
    private ApplicationService applicationService;

    @Override
    public ResponseEntity<ApplicationWarpRes> list(Application application) {
        ApplicationWarpRes warpRes = VoBaseResp.ok("查询成功", ApplicationWarpRes.class);
        Example<Application> example = Example.of(application);
        List<Application> applicationList = applicationService.list(example);
        if (CollectionUtils.isEmpty(applicationList)) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "查询失败",
                            ApplicationWarpRes.class));
        }
        Application application1 = applicationList.get(0);
        warpRes.setApplicationId(application1.getId());
        warpRes.setAliasName(application1.getAliasName());
        warpRes.setLogo(application1.getLogo());
        warpRes.setQrodeUrl(application1.getQrodeUrl());
        warpRes.setSketch(application1.getSketch());
        return ResponseEntity.ok(warpRes);
    }

}
