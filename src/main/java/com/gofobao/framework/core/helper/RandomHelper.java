package com.gofobao.framework.core.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * 随机工具类
 * Created by Max on 17/2/21.
 */
public class RandomHelper {

    private static Logger logger = LoggerFactory.getLogger(RandomHelper.class);
    /**
     * 数字串
     */
    public final static String NUMBER_STR = "0123456789";

    /**
     * 生成固定长度数字串
     *
     * @param length 长度
     * @return 随机数字串
     */
    public static String generateNumberCode(int length) {
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBER_STR.charAt(random.nextInt(NUMBER_STR.length())));
        }
        return sb.toString();
    }
}
