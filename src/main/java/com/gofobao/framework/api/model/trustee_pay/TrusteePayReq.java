package com.gofobao.framework.api.model.trustee_pay;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class TrusteePayReq extends JixinBaseRequest {
    /**
     *
     */
    private String accountId;
    /**
     * 标的编号
     */
    private String productId;
    /**
     * 借款人证件类型 01-身份证（18位）
     */
    private String idType;
    /**
     *  借款人证件号码
     */
    private String idNo;
    /**
     *收款人电子帐户
     */
    private String receiptAccountId;
    /**
     * 忘记密码的跳转URL
     */
    private String forgotPwdUrl;
    /**
     * 交易后台跳转的前台URL
     需要接收POST表单
     */
    private String retUrl;
    /**
     * 后台通知URL，“响应参数”返回到该URL，收到后返回“success”
     */
    private String notifyUrl;
    /**
     *
     */
    private String acqRes;
}
