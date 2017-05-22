package com.gofobao.framework.api.model.account_open_plus;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountOpenPlusRequest extends JixinBaseRequest {
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
     * 前导业务授权码
     */
    private String lastSrvAuthCode ;

    /**
     * 短信验证码
     *
     */
    private String smsCode ;

    /**
     * 客户IP
     */
    private String userIP;
    /**
     * 请求方保留
     */
    private String acqRes;
}
