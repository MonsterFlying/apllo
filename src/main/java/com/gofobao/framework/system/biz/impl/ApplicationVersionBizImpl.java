package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.biz.ApplicationVersionBiz;
import com.gofobao.framework.system.contants.VersionContants;
import com.gofobao.framework.system.entity.ApplicationVersion;
import com.gofobao.framework.system.entity.SysVersion;
import com.gofobao.framework.system.service.ApplicationVersionService;
import com.gofobao.framework.system.vo.response.VoSysVersion;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
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
    public void recheckVersion(ApplicationVersion applicationVersion, HttpServletResponse response) {
        //当前
        ApplicationVersion applicationVersion1 = new ApplicationVersion();
        applicationVersion1.setApplicationId(applicationVersion.getApplicationId());
        Example example = Example.of(applicationVersion1);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        PrintWriter printWriter = null;
        Map<String, Object> resultMap = Maps.newHashMap();
        Map<String, Object> statusMap = Maps.newHashMap();
        Map<String, Object> statusMaps = Maps.newHashMap();
        List<ApplicationVersion> applicationVersions = applicationVersionService.list(example,
                new Sort(Sort.Direction.DESC, "versionId"));
        try {
            printWriter = response.getWriter();
            if (CollectionUtils.isEmpty(applicationVersions)) {
                statusMaps.put("code", 1);
                statusMaps.put("msg", "非法访问");
                statusMaps.put("time", DateHelper.dateToString(new Date()));
                resultMap.put("state", statusMaps);
                printWriter.print(new Gson().toJson(statusMap));
            }
            ApplicationVersion sysVersion = applicationVersions.get(0);
            boolean flag = sysVersion.getVersionId() > applicationVersion.getVersionId();
            VoSysVersion voSysVersion = new VoSysVersion();
            if (flag) {  // 需要
                voSysVersion.setIsEquls(VersionContants.EQULSNO);
                voSysVersion.setIsNew(true);
                voSysVersion.setViewVersion(sysVersion.getViewVersion());
                voSysVersion.setDetails(sysVersion.getDescription());
                voSysVersion.setForce(sysVersion.getForce());
                voSysVersion.setUrl(sysVersion.getApplicationUrl());
            } else {   // 不需要
                voSysVersion.setIsEquls(VersionContants.EQULSOK);
                voSysVersion.setViewVersion(sysVersion.getViewVersion());
                voSysVersion.setDetails(sysVersion.getDescription());
                voSysVersion.setForce(sysVersion.getForce());
                voSysVersion.setIsNew(false);
            }
            statusMaps.put("code", 0);
            statusMaps.put("msg", "查询成功");
            statusMaps.put("time", DateHelper.dateToString(new Date()));
            resultMap.put("voSysVersion", voSysVersion);
            resultMap.put("state", statusMaps);
            printWriter.print(new Gson().toJson(resultMap));
        } catch (Exception e) {
            statusMaps.put("code", 1);
            statusMaps.put("msg", "系统异常, 请稍后重试");
            statusMaps.put("time", DateHelper.dateToString(new Date()));
            resultMap.put("state", statusMaps);
            printWriter.print(new Gson().toJson(statusMap));
        }
        return;
    }


}
