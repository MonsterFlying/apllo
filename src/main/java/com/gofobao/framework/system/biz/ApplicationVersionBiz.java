package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.vo.response.VoSysVersion2;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by master on 2017/10/23.
 */
public interface ApplicationVersionBiz {

    ResponseEntity<VoSysVersion2> recheckVersion(String aliasName, Integer versionId , HttpServletResponse response);

}
