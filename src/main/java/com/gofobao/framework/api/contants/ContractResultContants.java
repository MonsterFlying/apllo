package com.gofobao.framework.api.contants;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author master
 * @date 2017/11/14
 */
public class ContractResultContants {
    private static final Map<String, String> result = new HashMap<>();
    static {
        result.put("0000", "成功");
        result.put("9998", "验签失败");
        result.put("9999", "系统异常");
        result.put("0001", "文件名不合法");
        result.put("0002", "参数无法识别");
        result.put("0003", "文件过大");
        result.put("0004", "MD5校验失败");
        result.put("0005", "请勿重复上传");
        result.put("0006", "请勿上传空文件");
        result.put("0007", "交易日期须和文件名日期一致");
        result.put("0008", "文件名不正确");
        result.put("0009", "文件内容格式或者信息不正确");
        result.put("JX900004", "版本号错误");
        result.put("JX900054", "日期格式或内容不合法");
        result.put("JX900055", "时间格式或内容不合法");
        result.put("JX900056", "交易流水号超长");
        result.put("JX900057", "交易流水号存在非法字符");
        result.put("JX900005", "证件类型错误");
        result.put("JX900052", "手机号不合法");
        result.put("JX900016", "手机验证码发送失败");
        result.put("JX900630", "交易超时");
        result.put("JX900689", "交易中关联产品包为空值");
        result.put("JX100101", "机构号不能为空");
        result.put("JX100102", "版本号号不能为空");
        result.put("JX100103", "交易代码不能为空");
        result.put("JX100104", "交易日期不能为空");
        result.put("JX100105", "交易时间不能为空");
        result.put("JX100106", "交易流水不能为空");
        result.put("JX100107", "交易渠道不能为空");
        result.put("JX100108", "手机号码不能为空");
        result.put("JX100109", "业务交易代码不能为空");
        result.put("JX100110", "缺少必填字段");
        result.put("JX100111", "用户信息不存在");
        result.put("JX100112", "手机号于绑定的手机号不一致");
        result.put("JX100113", "手机验证码或授权码验证失败");
        result.put("JX100114", "未开通电子合同业务");
        result.put("JX100115", "账号为开户请联系客服");
        result.put("JX100116", "主动签署失败");
        result.put("JX100117", "未查询到标的绑定模版编号信息");
        result.put("JX100118", "未查询到模版信息");
        result.put("JX100119", "查询用户登陆信息手机号参数为空");

    }

    /**
     * 成功
     */
    public static final String SUCCESS = "0000";

    /**
     * Batch 成功
     */
    public static final String SUCCESS_STR = "success";


    /**
     * 是否需要
     *
     * @param jixinBaseResponse
     * @return
     */
    public static boolean toBeConfirm(JixinBaseResponse jixinBaseResponse) {
        if (ObjectUtils.isEmpty(jixinBaseResponse)) {
            return false;
        }

        for (String item : TO_BE_CONFIRM_ARRS) {
            if (jixinBaseResponse.getRetCode().equalsIgnoreCase(item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 请求重试
     */
    public static final String ERROR_CASH_WAIT = "CE999028";

    /**
     * 即信业务超时
     */
    public static final String ERROR_WAIT_CT9903 = "CT9903";

    /**
     * 即信业务超时
     */
    public static final String ERROR_WAIT_CT990300 = "CT990300";

    /**
     * 即信业务超时
     */
    public static final String ERROR_WAIT_CE999999 = "CE999999";

    /**
     * 即信返回异常
     */
    public static final String ERROR_WAIT_510000 = "510000";

    /**
     * 请求即信服务期错误
     */
    public static final String ERROR_502 = "ERROR_502";

    /**
     * 请求即信响应超时
     */
    public static final String ERROR_504 = "ERROR_504";

    /**
     * 通用请求错误
     */
    public static final String ERROR_COMMON_CONNECT = "ERROR_COMMON_CONNECT";

    /**
     * 验签失败
     */
    public static final String ERROR_SIGN = "ERROR_SIGN";

    /**
     * 访问频率超限
     */
    public static final String ERROR_JX900032 = "JX900032";
    /**
     * 交易未开通
     */
    public static final String ERROR_JX900663 = "JX900663";

    /**
     * 即信系统异常
     */
    public static final String ERROR_JX999999 = "JX999999";


    /**
     * 交易重复,请保证instCode,txDate,txTime,seqNo唯一
     */
    public static final String ERROR_JX900014 = "JX900014";


    /**
     * 是否网络错误
     *
     * @param jixinBaseResponse
     * @return true, 存在网络错误, false, 正常
     */
    public static boolean isNetWordError(JixinBaseResponse jixinBaseResponse) {
        if (ObjectUtils.isEmpty(jixinBaseResponse)) {
            return true;
        }

        if ((JixinResultContants.ERROR_COMMON_CONNECT.equalsIgnoreCase(jixinBaseResponse.getRetCode()))
                || (JixinResultContants.ERROR_502.equalsIgnoreCase(jixinBaseResponse.getRetCode()))
                || (JixinResultContants.ERROR_504.equalsIgnoreCase(jixinBaseResponse.getRetCode()))) {
            return true;
        }

        return false;
    }

    /**
     * 是否访问过于频繁, 建议线程休眠2秒
     *
     * @param jixinBaseResponse
     * @return
     */
    public static boolean isBusy(JixinBaseResponse jixinBaseResponse) {
        if (ObjectUtils.isEmpty(jixinBaseResponse)) {
            return true;
        }

        if ((JixinResultContants.ERROR_JX900032.equalsIgnoreCase(jixinBaseResponse.getRetCode()))) {
            return true;
        }

        return false;
    }

    public static final String[] TO_BE_CONFIRM_ARRS;

    static {
        TO_BE_CONFIRM_ARRS = new String[]{
                ERROR_CASH_WAIT,
                ERROR_502,
                ERROR_504,
                ERROR_WAIT_510000,
                ERROR_WAIT_CE999999,
                ERROR_WAIT_CT9903,
                ERROR_WAIT_CT990300
        };

    }

    public static String getMessage(String retCode) {
        String message = result.get(retCode);
        Optional<String> rs = Optional.ofNullable(message);
        return rs.orElse("未知错误,请通知客户");
    }

}
