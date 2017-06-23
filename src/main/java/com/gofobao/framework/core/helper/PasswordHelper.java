package com.gofobao.framework.core.helper;

import org.springframework.util.StringUtils;

/**
 * 密码帮助类
 * Created by Max on 17/2/15.
 */
public class PasswordHelper {

    /**
     * 加密用户密码
     *
     * @param password 用户密码
     * @return 已加密密码
     * @throws Exception
     */
    public static String encodingPassword(String password) throws Exception {
        if (StringUtils.isEmpty(password)) {
            throw new NullPointerException("待加密字符串为空");
        }

        return BCrypt.hashpw(password, BCrypt.gensalt());
    }


    /**
     * 验证用户密码是否正确
     *
     * @param origPassword 用户密码
     * @param password     待验证密码
     * @return treu：密码验证通过； false： 密码验证失败
     * @throws Exception
     */
    public static boolean verifyPassword(String origPassword, String password) throws Exception {
        if ((StringUtils.isEmpty(origPassword)) || (StringUtils.isEmpty(password))) {
            throw new NullPointerException("用户密码/待验证密码为空");
        }
        return BCrypt.checkpw(password, origPassword);
    }
}
