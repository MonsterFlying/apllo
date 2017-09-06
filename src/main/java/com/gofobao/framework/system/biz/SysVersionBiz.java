package com.gofobao.framework.system.biz;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/6/21.
 */

public interface SysVersionBiz {


     void list(Integer terminal, Integer clientId, HttpServletResponse response);
}
