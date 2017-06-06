package com.gofobao.framework.common.response;



/**
 * 通用响应状态常量类集合[0 - 200 ]
 * Created by Max on 17/2/14.
 */
public class CommonRespMsgStatesContants {
    /**
     * 操作成功
     */
    public static final AbstractRespMsgState SUCCESS = new CommonRespMsgState(0, "操作成功");

    /**
     * 操作失败
     */
    public static final AbstractRespMsgState FAILURE = new CommonRespMsgState(1, "操作失败");

    /**
     * 操作不存在
     */
    public static final AbstractRespMsgState OPERATION_NOT_EXIST = new CommonRespMsgState(2, "操作不存在");
/*

    */
/**
     * 服务器异常
     *//*

    public static final AbstractRespMsgState ERROR = new UserRespMsgState(3, "服务器开小差了，请稍候重试！");

    */
/**
     * token缺少必要参数
     *//*

    public static final AbstractRespMsgState TOKEN_LACK_PARAM = new UserRespMsgState(4, "token缺少必要参数");

    */
/**
     * token验证不通过
     *//*

    public static final AbstractRespMsgState TOKEN_CHECK_FAILURE = new UserRespMsgState(5, "登陆已经过期，请重新登录！");

    */
/**
     * 缺少必填参数
     *//*

    public static final AbstractRespMsgState REQUIRED_PARAM = new UserRespMsgState(6, "你访问了不该访问的地方，请停止操作");
*/

    /**
     * 验证通过
     */
    public static final AbstractRespMsgState CHECK_PASS = new CommonRespMsgState(7, "验证通过");

    /**
     * 验证不通过
     */
    public static final AbstractRespMsgState CHECK_NO_PASS = new CommonRespMsgState(8, "验证不通过");

    /**
     * 参数不合规
     */
    public static final AbstractRespMsgState PARAMS_ERROR = new CommonRespMsgState(9, "请停止请求，你的操作姿势有问题！");

    /**
     * 非法访问
     */
    public static final AbstractRespMsgState ACCESS_INVAILE = new CommonRespMsgState(10, "啊，请不要乱点");


    /**
     * 手机号码已存在
     */
    public static final AbstractRespMsgState PHONE_IS_EXIST = new CommonRespMsgState(10, "手机号码已存在");

    /**
     * 冻结操作
     */
    public static final AbstractRespMsgState COMMON_FREEZE = new CommonRespMsgState(11, "当前操作过于频繁，系统已经拒绝该操作！");


    /**
     * 账户已存在
     */
    public static final AbstractRespMsgState EXISTING = new CommonRespMsgState(12, "账户已存在");


    /**
     * 图形验证码输入错误，请刷新重试!
     */
    public static final AbstractRespMsgState CAPTCHA_ERROR = new CommonRespMsgState(13, "图形验证码输入错误，请刷新重试!");

    /**
     * 验证码已过期
     */
    public static final AbstractRespMsgState CODE_INVALIDATE = new CommonRespMsgState(14, "验证码已过期");

    /**
     * 验证码错误
     */
    public static final AbstractRespMsgState CODE_ERROR = new CommonRespMsgState(15, "验证码错误");

    /**
     * 短信调用间隔小于60秒 （间隔60秒）
     */
    public static final AbstractRespMsgState SEND_SMS_OFTEN = new CommonRespMsgState(16, "短信发送间隔必须大于60秒");

    /**
     * 冻结当前IP
     */
    public static final AbstractRespMsgState IP_FREEZE = new CommonRespMsgState(17, "当前网络请求过于频繁，当前操作已冻结，请24小时后重试");


    /**
     * 请求出现参数错误，请调整参数
     */
    public static final AbstractRespMsgState SYSTEM_EXCEPION = new CommonRespMsgState(44, "当前请求出现异常，请更正请求参数");


}
