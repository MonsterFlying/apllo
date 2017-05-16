package com.gofobao.framework.api.model.openusers;

import com.gofobao.framework.api.request.AbsRequest;

/**
 * Created by Zeke on 2017/5/16.
 */
public class OpenUserRequest extends AbsRequest {
    /**
     * 证件类型
     */
    private String idType;
    /**
     * 证件号码
     */
    private String idNo;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 银行卡号
     */
    private String cardNo;
    /**
     * 邮箱
     */
    private String email;
    /**
     * acctUse
     */
    private String acctUse;
    /**
     * 前台跳转链接
     */
    private String retUrl;
    /**
     * 后台通知连接
     */
    private String notifyUrl;
    /**
     * 客户IP
     */
    private String userIP;
    /**
     * 请求方保留
     */
    protected String acqRes;
}
