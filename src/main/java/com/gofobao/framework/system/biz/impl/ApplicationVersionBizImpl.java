package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.biz.ApplicationVersionBiz;
import com.gofobao.framework.system.entity.ApplicationVersion;
import com.gofobao.framework.system.service.ApplicationVersionService;
import com.gofobao.framework.system.vo.response.VoSysVersion2;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by master on 2017/10/23.
 */
@Service
public class ApplicationVersionBizImpl implements ApplicationVersionBiz {

    @Autowired
    private ApplicationVersionService applicationVersionService;

    @Override
    public ResponseEntity<VoSysVersion2> recheckVersion(Integer applicationId, Integer versionId, HttpServletResponse response) {
        VoSysVersion2 voSysVersion = VoBaseResp.ok("查询成功", VoSysVersion2.class);
        try {
            printWriter= response.getWriter();
            //比较版本
            ApplicationVersion applicationVersion = new ApplicationVersion();
            applicationVersion.setApplicationId(applicationId);

            Example<ApplicationVersion> versionExample = Example.of(applicationVersion);
            List<ApplicationVersion> applicationVersions = applicationVersionService.list(versionExample,
                    new Sort(Sort.Direction.DESC, "versionId"));

            if (CollectionUtils.isEmpty(applicationVersions)) {
                throw new Exception();
            }
            ApplicationVersion sysVersion=applicationVersions.get(0);
            applicationVersion.setVersionId(versionId);
            boolean flag = sysVersion.getVersionId() > applicationVersion.getVersionId();
            // 需要
            if (flag) {
                voSysVersion.setUpgrade(true);
                voSysVersion.setViewVersion(sysVersion.getViewVersion());
                voSysVersion.setDetails(sysVersion.getDescription());
                voSysVersion.setForce(sysVersion.getForce());
                voSysVersion.setUrl(sysVersion.getApplicationUrl());
                /* 不需要 */
            } else {
                voSysVersion.setViewVersion(sysVersion.getViewVersion());
                voSysVersion.setDetails(sysVersion.getDescription());
                voSysVersion.setForce(sysVersion.getForce());
                voSysVersion.setUpgrade(false);
            }
            return ResponseEntity.ok(voSysVersion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(voSysVersion);
        }

    }

}
