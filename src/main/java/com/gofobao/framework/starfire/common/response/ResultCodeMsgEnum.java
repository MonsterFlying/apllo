package com.gofobao.framework.starfire.common.response;

/**
 * Created by master on 2017/9/26.
 */
public enum ResultCodeMsgEnum {

    //成功
    SUCCESS("0", "成功"),

    //未通过安全校验
    CHECK_SIGN_NO_PASS("0001", "未通过安全校验"),

    //其他错误
    OTHER_ERROR("0002", "其他错误"),

    // 未注册
    NOT_EXIST_REGISTER("1000 ", "未注册"),

    //已注册，未绑定任何渠道（含星火智投）
    EXIST_NO_BIND("1001", "已注册，未绑定任何渠道（含星火智投）"),

    //  已注册，已绑定星火智投的引流用户(通过星火智投新注册平台的用户)
    EXIST_AND_BIND_STAR_FRIE("1002", "已注册，已绑定星火智投的引流用户(通过星火智投新注册平台的用户)"),

    // 已注册，已绑定星火智投的唤醒用户(非通过星火智投注册平台，但绑定智投账号的用户)
    EXIST_AND_BIND_STAR_FRIE_NO("1003", "已注册，已绑定星火智投的唤醒用户(非通过星火智投注册平台，但绑定智投账号的用户"),

    // 已注册，其他渠道用户
    OTHER_CHANNEL("1004", "已注册，其他渠道用户"),

    // 已注册，已绑定星火智投的其他渠道用户
    OTHER_CHANNEL_FRIE("1005", "已注册，已绑定星火智投的其他渠道用户"),

    //注册成功，绑定星火智投失败
    REGISTER_SUCCESS_BIND_FIRE_FAIL("2001", "注册成功，绑定星火智投失败"),

    //注册失败，该用户已存在
    FAIL_USER_EXIST("2002", "注册失败，该用户已存在"),

    //注册失败，其他原因
    FAIL_OTHER("2003", "注册失败，其他原因"),

    //绑定失败
    BIND_FAIL("3001", "绑定失败"),

    //获取 login_token 失败，用户信息校验错误
    GOT_LOGIN_TOKEN_FAIL("4001", " 获取 login_token 失败，用户信息校验错误");

    private String code;
    private String resultMsg;

    ResultCodeMsgEnum(String code, String resultMsg) {
        this.code = code;
        this.resultMsg = resultMsg;
    }

    /**
     * 获取状态说明
     *
     * @param code
     * @return
     */
    public static String getResultMsg(String code) {
        for (ResultCodeMsgEnum c : ResultCodeMsgEnum.values()) {
            if (c.code.equals(code)) {
                return c.resultMsg;
            }
        }
        return null;
    }

}
