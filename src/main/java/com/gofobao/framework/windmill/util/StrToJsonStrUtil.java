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
    /**
     * 解密并封装参数
     *
     * @param cipherStr
     * @return
     */
    public static Map<String, Object> commonUrlParamToMap(String cipherStr, String desKey) throws Exception {
        String desDecryptStr = WrbCoopDESUtil.desDecrypt(desKey, cipherStr);
        if (StringUtils.isEmpty(desDecryptStr)) {
            throw new Exception("请求密文为空");
        }

        return getUrlParams(desDecryptStr);
    }

    /**
     * 将url参数转换成map
     *
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
            for (int j = 0; j < p.length; j++) {
                if (p.length == 1) {
                    map.put(p[0], null);
                } else {
                    map.put(p[0], p[1]);
                }
            }
        }
        return map;
    }

}




