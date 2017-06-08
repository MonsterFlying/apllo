package com.gofobao.framework.common.integral;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zeke on 2017/6/7.
 */
@Data
public class IntegralChangeConfig {
    /**
     * 积分折现名
     */
    private String name;
    /**
     * 积分
     */
    private String integralChangeRule;
    /**
     * 类型
     */
    private IntegralChangeEnum type;

    /**
     * 规则类
     */
    public final static List<IntegralChangeConfig> integralChangeList = new ArrayList<>();

    static {
        //投资积分
        IntegralChangeConfig tender = new IntegralChangeConfig();
        tender.setName("投资积分");
        tender.setType(IntegralChangeEnum.TENDER);
        tender.setIntegralChangeRule("add@useIntegral");
        integralChangeList.add(tender);
        //积分折现
        IntegralChangeConfig convert = new IntegralChangeConfig();
        convert.setName("积分折现");
        convert.setType(IntegralChangeEnum.CONVERT);
        convert.setIntegralChangeRule("sub@useIntegral,add@no_useIntegral");
        integralChangeList.add(convert);
        //发帖积分
        IntegralChangeConfig post = new IntegralChangeConfig();
        post.setName("发帖积分");
        post.setType(IntegralChangeEnum.POST);
        post.setIntegralChangeRule("add@useIntegral");
        integralChangeList.add(post);
        //回帖积分
        IntegralChangeConfig reply = new IntegralChangeConfig();
        reply.setName("回帖积分");
        reply.setType(IntegralChangeEnum.REPLY);
        reply.setIntegralChangeRule("add@useIntegral");
        integralChangeList.add(reply);
        //加精华积分
        IntegralChangeConfig digest = new IntegralChangeConfig();
        digest.setName("加精华积分");
        digest.setType(IntegralChangeEnum.DIGEST);
        digest.setIntegralChangeRule("add@useIntegral");
        integralChangeList.add(digest);
        //取消精华积分
        IntegralChangeConfig _digest = new IntegralChangeConfig();
        _digest.setName("取消精华积分");
        _digest.setType(IntegralChangeEnum._DIGEST);
        _digest.setIntegralChangeRule("sub@useIntegral");
        integralChangeList.add(_digest);
        //签到积分
        IntegralChangeConfig sign = new IntegralChangeConfig();
        sign.setName("签到积分");
        sign.setType(IntegralChangeEnum.SIGN);
        sign.setIntegralChangeRule("add@useIntegral");
        integralChangeList.add(sign);
        //投资积分
        IntegralChangeConfig signAward = new IntegralChangeConfig();
        signAward.setName("签到奖励积分");
        signAward.setType(IntegralChangeEnum.SIGN_AWARD);
        signAward.setIntegralChangeRule("add@useIntegral");
        integralChangeList.add(signAward);
    }
}
