package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.entity.ApplicationVersion;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by master on 2017/10/23.
 */
public interface ApplicationVersionBiz {

    void recheckVersion(ApplicationVersion applicationVersion,HttpServletResponse response);

}
