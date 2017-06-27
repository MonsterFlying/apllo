package com.gofobao.framework.helper.project;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

/**
 * Created by Zeke on 2017/6/19.
 */
@Slf4j
public class SecurityHelper {
    private final static String requestPrefix = "gfb_req_";

    public static boolean checkSign(String sign, String paramStr) {
        if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(paramStr)) {
            log.info("SecurityHelper detail：缺少必填请求参数！");
            return false;
        }
        return sign.equals(getSign(paramStr));
    }

    public static String getSign(String paramStr) {
        return DigestUtils.md5Hex((requestPrefix + paramStr).getBytes());
    }
}
