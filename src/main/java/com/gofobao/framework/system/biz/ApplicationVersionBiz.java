package com.gofobao.framework.system.biz;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by master on 2017/10/23.
 */
public interface ApplicationVersionBiz {

    void recheckVersion(Integer aliasName,Integer versionId ,HttpServletResponse response);

}
