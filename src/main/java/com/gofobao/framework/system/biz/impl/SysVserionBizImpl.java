package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.biz.SysVersionBiz;
import com.gofobao.framework.system.contants.VersionContants;
import com.gofobao.framework.system.entity.SysVersion;
import com.gofobao.framework.system.service.SysVersionService;
import com.gofobao.framework.system.vo.response.VoSysVersion;
import com.google.common.collect.Maps;
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
        Map<String,Object> result= Maps.newHashMap();
        Map<String,Object>resultMaps=Maps.newHashMap();
        try {
            printWriter= response.getWriter();
            if (CollectionUtils.isEmpty(sysVersions)) {
                resultMaps.put("code",1);
                resultMaps.put("msg","非法访问");
                resultMaps.put("time", DateHelper.dateToString(new Date()));
                result.put("status",resultMaps);
                printWriter.print(result);
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
            resultMaps.put("code",1);
            resultMaps.put("msg","查询成功");
            resultMaps.put("time", DateHelper.dateToString(new Date()));
            resultMaps.put("body",voSysVersion);
            result.put("status",resultMaps);
            printWriter.print(result);
        }catch (Exception e){
            resultMaps.put("code",1);
            resultMaps.put("msg","系统异常, 请稍后重试");
            resultMaps.put("time", DateHelper.dateToString(new Date()));
            result.put("status",resultMaps);
            printWriter.print(result);

        }


    }
}
