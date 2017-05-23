package com.gofobao.framework.helper;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 图形验证码帮助类
 * Created by Max on 17/5/17.
 */
@Component
@Slf4j
public class CaptchaHelper {

    public static final String REDIS_CAPTCHA_PREFIX_KEY = "captcha_";

    @Autowired
    RedisHelper redisHelper;


    /**
     * 判断验证码是否正确（验证函数后都会删除图形验证码）
     *
     * @param key     redis key
     * @param captcha 验证码
     * @return boolean
     */
    public boolean match(String key, String captcha) {
        try {
            String redisKey = String.format("%s%s", REDIS_CAPTCHA_PREFIX_KEY, key);

            String redisCaptcha = redisHelper.get(redisKey, null);
            redisHelper.remove(redisKey);
            if (Objects.isNull(redisCaptcha)) {
                return false;
            }

            return captcha.equalsIgnoreCase(redisCaptcha);
        } catch (Exception e) {
            log.error("CaptchaHelper match exception", e);
        }

        return false;
    }

    /**
     * 添加验证码
     *
     * @param key     redis key
     * @param captcha 验证码
     * @return boolean
     */
    public boolean add(String key, String captcha) {
        try {
            redisHelper.put(String.format("%s%s", REDIS_CAPTCHA_PREFIX_KEY, key), captcha, 15 * 60);
            return true;
        } catch (Exception e) {
            log.error("CaptchaHelper add exception", e);
        }
        return false;
    }

    /**
     * 校验忘记密码手机验证码
     *
     * @param phone
     * @param phoneCaptcha
     * @param SMSType
     * @return 0成功 1手机验证码失效 2.手机验证码错误
     */
    public boolean checkPhoneCaptcha(String phone, String phoneCaptcha, String SMSType) {
        String checkPhoneCaptcha = null;
        boolean bool = false;

        try {
            checkPhoneCaptcha = redisHelper.get(String.format("%s_%s", SMSType, phone), "");
        } catch (Exception e) {
            log.error("CaptchaHelper phoneCaptcha not exist:", e);
        }

        if (checkPhoneCaptcha.equals(phoneCaptcha)) {
            bool = true;
        }

        return bool;
    }

    /**
     * 校验忘记密码手机验证码
     *
     * @param phone
     * @param SMSType
     * @return 0成功 1手机验证码失效 2.手机验证码错误
     */
    public boolean removePhoneCaptcha(String phone,String SMSType) {
        try {
            redisHelper.remove(String.format("%s_%s", SMSType, phone));
            return true;
        } catch (Exception e) {
            log.error("CaptchaHelper add exception", e);
        }
        return false;
    }
}
