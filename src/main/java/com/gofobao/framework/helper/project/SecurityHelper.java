package com.gofobao.framework.helper.project;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

/**
 * Created by Zeke on 2017/6/19.
 */
@Slf4j
public class SecurityHelper {
    private final static String requestPrefix = "gfb_req_";

    public static boolean checkRequest(String sign, String paramStr) {
        if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(paramStr)) {
            log.info("SecurityHelper info：缺少必填请求参数！");
            return false;
        }
        return sign.equals(Base64Utils.encodeToString((requestPrefix + paramStr).getBytes()));
    }
}
