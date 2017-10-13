package com.gofobao.framework.starfire.util;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Created by master on 2017/9/26.
 */


@Slf4j
public class SignUtil {
    /**
     * 解签
     *
     * @param baseRequest
     * @return
     */
    public static boolean checkSign(BaseRequest baseRequest, String key, String initVector) {
        if (ObjectUtils.isEmpty(baseRequest)) {
            return false;
        }
        log.info("==============进入解签方法===========");
        log.info("打印待验证签名参数:" + new Gson().toJson(baseRequest));
        String sign = baseRequest.getSign();
        log.info("打印当前待解签字符串:" + sign);
        String serialNum = baseRequest.getSerial_num();
        String cCode = baseRequest.getC_code();
        String tCode = baseRequest.getT_code();
        //验证参数
        if (StringUtils.isEmpty(sign)
                || StringUtils.isEmpty(serialNum)
                || StringUtils.isEmpty(cCode)
                || StringUtils.isEmpty(tCode)) {
            return false;
        }
        //解密参数 并 拼借参数
        String signStr = serialNum
                + AES.decrypt(key, initVector, cCode)
                + AES.decrypt(key, initVector, tCode)
                + key;
        //加密获取sign;
        String generateSignStr = Sha256.Encrypt(signStr, "");
        log.info("平台获取参数后生成签名:" + generateSignStr);
        //验证对比签名
        if (!sign.equals(generateSignStr)) {
            log.info("签名验证失败");
            return false;
        } else {
            log.info("签名验证成功");
            return true;
        }


    }

}
