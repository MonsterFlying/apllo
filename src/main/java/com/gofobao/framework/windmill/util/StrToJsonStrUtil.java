package com.gofobao.framework.windmill.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * Created by admin on 2017/8/3.
 */
@Slf4j
public class StrToJsonStrUtil {


    //加密key
    @Value("${windmill.des-key}")
    private static String desKey;

    /**
     * @param cipherStr
     * @return
     */
    public static String commonDecryptStr(String cipherStr) throws Exception {
        log.info("-==========进入解密请求参数方法========");
        try {
            String desDecryptStr = WrbCoopDESUtil.desDecrypt(desKey, cipherStr);
            if (StringUtils.isEmpty(desDecryptStr)) {
                log.info("請求密文為空");
                throw new Exception();
            }
            String jsonStr = desDecryptStr.replaceAll("&", ",").replaceAll("=", ":");
            log.info(jsonStr);
            StringBuilder sb = new StringBuilder(jsonStr);
            return "{" + sb.toString() + "}";
        } catch (Exception e) {
            log.info("密文解析失败");
            throw new Exception();
        }
    }

}
