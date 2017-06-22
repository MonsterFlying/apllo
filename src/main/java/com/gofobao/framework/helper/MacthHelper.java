package com.gofobao.framework.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@Component
@Slf4j
public class MacthHelper {

    @Autowired
    RedisHelper redisHelper ;


    /**
     * 判断短信验证码
     * @param tag tag
     * @param phone 手机号
     * @param smsCode 短信验证码
     * @return
     */
    public boolean match(String tag, String phone, String smsCode) {
        try {
            String redisKey = String.format("%s%s",tag, phone);
            String redisSmsCode = redisHelper.get(redisKey, null);
            redisHelper.remove(redisKey);
            if (Objects.isNull(redisSmsCode)) {
                return false;
            }
            return smsCode.equalsIgnoreCase(redisSmsCode);
        } catch (Exception e) {
            log.error("SmsHelper match exception", e);
        }
        return false;
    }

    /**
     * 判断短信验证码,但不删除短信
     * @param tag tag
     * @param phone 手机号
     * @param smsCode 短信验证码
     * @return
     */
    public boolean matchAndNoRemove(String tag, String phone, String smsCode) {
        try {
            String redisKey = String.format("%s%s",tag, phone);
            String redisSmsCode = redisHelper.get(redisKey, null);
            if (Objects.isNull(redisSmsCode)) {
                return false;
            }
            return smsCode.equalsIgnoreCase(redisSmsCode);
        } catch (Exception e) {
            log.error("SmsHelper match exception", e);
        }
        return false;
    }


    public boolean add(String tag, String phone, String smsCode) {
        try {
            String redisKey = String.format("%s%s",tag, phone);
            redisHelper.put(redisKey, smsCode, 15 * 60);
            return true;
        } catch (Exception e) {
            log.error("SmsHelper add exception", e);
        }
        return false;
    }
}
