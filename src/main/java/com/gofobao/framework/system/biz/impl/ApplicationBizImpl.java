package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.ApplicationBiz;
import com.gofobao.framework.system.entity.Application;
import com.gofobao.framework.system.service.ApplicationService;
import com.gofobao.framework.system.vo.response.ApplicationWarpRes;
import com.google.common.collect.Lists;
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

    /**
     * @param application
     * @return
     */
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
        List<ApplicationWarpRes.VoApplication> applications = Lists.newArrayList();
        applicationList.forEach(application1 -> {
                    ApplicationWarpRes.VoApplication voApplication = warpRes.new VoApplication();
                    voApplication.setApplicationId(application1.getId());
                    voApplication.setAliasName(application1.getAliasName());
                    voApplication.setLogo(application1.getLogo());
                    voApplication.setQrodeUrl(application1.getQrodeUrl());
                    voApplication.setSketch(application1.getSketch());
                    voApplication.setTerminal(application1.getTerminal());
                    applications.add(voApplication);
                }
        );
        warpRes.setApplications(applications);
        return ResponseEntity.ok(warpRes);
    }

}
