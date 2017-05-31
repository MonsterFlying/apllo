package com.gofobao.framework.helper;

import com.google.common.base.Preconditions;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Max on 17/5/31.
 */
public class RequestHelper {

    /**
     * 获取请求链接
     * @param request 请求类
     * @return 链接
     */
    public static String getUrl(HttpServletRequest request) {
        Preconditions.checkNotNull(request,
                "RequestHelper getUrl: request is null") ;
        return request.getRequestURL().toString() ;
    }
}
