package com.gofobao.framework.starfire.common.response;

/**
 * Created by master on 2017/9/26.
 */
public enum ResultCodeEnum {

    //成功
    SUCCESS("SUCCESS", "0"),

    //未通过安全校验
    CHECK_SIGN_NO_PASS("CHECK_SIGN_NO_PASS", "0001"),

    //其他错误
    OTHER_ERROR("OTHER_ERROR", "0002"),

    // 未注册    NOT_EXIST_REGISTER("NOT_EXIST_REGISTER", "1000"),

    //已注册，未绑定任何渠道（含星火智投）
    EXIST_NO_BIND("EXIST_NO_BIND", "1001"),

    //  已注册，已绑定星火智投的引流用户(通过星火智投新注册平台的用户)
    EXIST_AND_BIND_STAR_FIRE("EXIST_AND_BIND_STAR_FIRE", "1002"),

    // 已注册，已绑定星火智投的唤醒用户(非通过星火智投注册平台，但绑定智投账号的用户)
    EXIST_AND_BIND_STAR_FIRE_NO("EXIST_AND_BIND_STAR_FIRE_NO", "1003"),

    // 已注册，其他渠道用户
    OTHER_CHANNEL("OTHER_CHANNEL", "1004"),

    // 已注册，已绑定星火智投的其他渠道用户
    OTHER_CHANNEL_FIRE("OTHER_CHANNEL_FIRE", "1005"),

    //注册成功，绑定星火智投失败
    REGISTER_SUCCESS_BIND_FIRE_FAIL("REGISTER_SUCCESS_BIND_FIRE_FAIL", "2001"),

    //注册失败，该用户已存在
    FAIL_USER_EXIST("FAIL_USER_EXIST", "2002"),

    //注册失败，其他原因
    FAIL_OTHER("FAIL_OTHER", "2003"),

    //绑定失败
    BIND_FAIL("BIND_FAIL", "3001"),

    //获取 login_token 失败，用户信息校验错误
    GOT_LOGIN_TOKEN_FAIL("GOT_LOGIN_TOKEN_FAIL", "4001");

    private String code;
    private String resultMsg;

    ResultCodeEnum(String code, String resultMsg) {
        this.code = code;
        this.resultMsg = resultMsg;
    }

    /**
     * 获取状态说明
     *
     * @param code
     * @return
     */
    public static String getCode(String code) {
        for (ResultCodeEnum c : ResultCodeEnum.values()) {
            if (c.code.equals(code)) {
                return c.resultMsg;
            }
        }
        return null;
    }

}
