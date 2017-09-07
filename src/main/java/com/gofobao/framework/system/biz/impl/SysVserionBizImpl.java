package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.biz.SysVersionBiz;
import com.gofobao.framework.system.contants.VersionContants;
import com.gofobao.framework.system.entity.SysVersion;
import com.gofobao.framework.system.service.SysVersionService;
import com.gofobao.framework.system.vo.response.VoSysVersion;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/21.
 */
@Service
public class SysVserionBizImpl implements SysVersionBiz {
    @Autowired
    private SysVersionService sysVersionService;

    @Override
    public void list(Integer terminal, Integer clientId, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        List<SysVersion> sysVersions = sysVersionService.list(terminal);
        PrintWriter printWriter=null;
        Map<String,Object>resultMap=Maps.newHashMap();
        Map<String,Object> statusMap= Maps.newHashMap();
        Map<String,Object>statusMaps=Maps.newHashMap();
        try {
            printWriter= response.getWriter();
            if (CollectionUtils.isEmpty(sysVersions)) {
                statusMaps.put("code",1);
                statusMaps.put("msg","非法访问");
                statusMaps.put("time", DateHelper.dateToString(new Date()));
                resultMap.put("status",statusMaps);
                printWriter.print(new Gson().toJson(statusMap));
            }
            SysVersion sysVersion = sysVersions.get(0);
            boolean flag = sysVersion.getVersionId() > clientId;
            VoSysVersion voSysVersion = new VoSysVersion();
            if (flag) {  // 需要
                voSysVersion.setIsEquls(VersionContants.EQULSNO);
                voSysVersion.setIsNew(true);
                voSysVersion.setVeiwVersion(sysVersion.getViewVersion());
                voSysVersion.setDetails(sysVersion.getDetails());
                voSysVersion.setForce(sysVersion.getForce());
                voSysVersion.setUrl(sysVersion.getRul());
            } else {   // 不需要
                voSysVersion.setIsEquls(VersionContants.EQULSOK);
                voSysVersion.setVeiwVersion(sysVersion.getViewVersion());
                voSysVersion.setDetails(sysVersion.getDetails());
                voSysVersion.setForce(sysVersion.getForce());
                voSysVersion.setIsNew(false);
            }
            statusMaps.put("code",0);
            statusMaps.put("msg","查询成功");
            statusMaps.put("time", DateHelper.dateToString(new Date()));
            resultMap.put("body",voSysVersion);
            resultMap.put("status",statusMaps);
            printWriter.print(new Gson().toJson(resultMap));
        }catch (Exception e){
            statusMaps.put("code",1);
            statusMaps.put("msg","系统异常, 请稍后重试");
            statusMaps.put("time", DateHelper.dateToString(new Date()));
            resultMap.put("status",statusMaps);
            printWriter.print(new Gson().toJson(statusMap));
        }


    }
}
