package com.gofobao.framework.helper;

import com.google.common.base.Preconditions;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Max on 17/5/31.
 */
public class HttpHelper {

    /**
     * 获取请求链接
     *
     * @param request 请求类
     * @return 链接
     */
    public static String getUrl(HttpServletRequest request) {
        Preconditions.checkNotNull(request,
                "HttpHelper getUrl: request is null");
        return request.getRequestURL().toString();
    }

    /**
     * 获取请求参数
     * @param request
     * @return
     */
    public static String getRequestParameter(HttpServletRequest request) {

        if (null == request) {
            return null;
        }

        String method = request.getMethod();
        String param = null;
        if (method.equalsIgnoreCase("GET")) {
            param = request.getQueryString();
            if (Base64.isBase64(param)) {
                param = new String(Base64.decodeBase64(param), StandardCharsets.UTF_8);
            }
        } else {
            param = getBodyData(request);
            if (Base64.isBase64(param)) {
                param = new String(Base64.decodeBase64(param), StandardCharsets.UTF_8);
            }
        }
        return param;
    }

    /**
     * 获取请求体中的字符串(POST)
     */
    private static String getBodyData(HttpServletRequest request) {
        StringBuffer data = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            while (null != (line = reader.readLine())) {
                data.append(line);
            }
        } catch (IOException e) {
        } finally {
        }
        return data.toString();
    }
}
