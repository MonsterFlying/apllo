package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.ApplicationVersionBiz;
import com.gofobao.framework.system.entity.ApplicationVersion;
import com.gofobao.framework.system.service.ApplicationVersionService;
import com.gofobao.framework.system.vo.response.VoSysVersion2;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
@Service
@Slf4j
public class ApplicationVersionBizImpl implements ApplicationVersionBiz {

    @Autowired
    private ApplicationVersionService applicationVersionService;

    @Override
    public ResponseEntity<VoSysVersion2> recheckVersion(String aliasName, Integer versionId, HttpServletResponse response) {
        Preconditions.checkState(!StringUtils.isEmpty(aliasName),  "params aliasName is empty");
        Preconditions.checkState(!ObjectUtils.isEmpty(versionId),  "params versionId is empty");

        try {
            ApplicationVersion applicationVersion = applicationVersionService.getNewApplicationVersion(aliasName) ;
            Preconditions.checkNotNull(applicationVersion, "applicationVersion record is null") ;
            Integer systemNewVersiom = applicationVersion.getVersionId();
            VoSysVersion2 voSysVersion = VoBaseResp.ok("查询成功", VoSysVersion2.class);
            if(systemNewVersiom > versionId){
                // 系统最新版本大于请求版本
                voSysVersion.setUpgrade(true);
                voSysVersion.setViewVersion(applicationVersion.getViewVersion());
                voSysVersion.setDetails(applicationVersion.getDescription());
                voSysVersion.setForce(applicationVersion.getForce());
                voSysVersion.setUrl(applicationVersion.getApplicationUrl());
            }else{
                // 无需更新
                voSysVersion.setViewVersion("");
                voSysVersion.setDetails("");
                voSysVersion.setForce(0);
                voSysVersion.setUpgrade(false);
                voSysVersion.setUrl("");
            }

            return ResponseEntity.ok(voSysVersion);
        } catch (Exception e) {
            log.error("版本检测异常", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(1,"查询失败",VoSysVersion2.class));
        }

    }

}
