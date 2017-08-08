package com.gofobao.framework.windmill.util;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/8/3.
 */
@Slf4j
public class StrToJsonStrUtil {

    private static final Gson GSON = new Gson();
    /**
     * @param cipherStr
     * @return
     */
    public static Map<String, Object> commonUrlParamToMap(String cipherStr, String desKey) throws Exception {
        log.info("-==========进入解密请求参数方法========");
        try {
            String desDecryptStr = WrbCoopDESUtil.desDecrypt(desKey, cipherStr);
            if (StringUtils.isEmpty(desDecryptStr)) {
                log.info("請求密文為空");
                throw new Exception();
            }
           return getUrlParams(desDecryptStr);

        } catch (Exception e) {
            log.info("密文解析失败");
            throw new Exception();
        }
    }

    /**
     * 将url参数转换成map
     * @param param aa=11&bb=22&cc=33
     * @return
     */
    public static Map<String, Object> getUrlParams(String param) {
        Map<String, Object> map = new HashMap<String, Object>(0);
        if (StringUtils.isEmpty(param)) {
            return map;
        }
        String[] params = param.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            for (int j = 0; j < p.length ; j++) {
                map.put(p[0], StringUtils.isEmpty(p[1])?"":p[1]);
            }
        }
        log.info("风车理财请求平台 解密后的param参数:"+GSON.toJson(map));
        return map;
    }

}




