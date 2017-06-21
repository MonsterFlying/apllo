package com.gofobao.framework.api.model.trustee_pay_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/21.
 */
@Data
public class TrusteePayQueryResp extends JixinBaseResponse{
    /**
     * 借款人电子账号
     */
    private String accountId;
    /**
     * 借款人姓名
     */
    private String name;
    /**
     * 标的号
     */
    private String productId;
    /**
     * 标的描述
     */
    private String productDesc;
    /**
     * 借款人证件类型
     */
    private String idType;
    /**
     * 借款人证件号码
     */
    private String idNo;
    /**
     * 收款方电子账号
     */
    private String receiptAccountId;
    /**
     * 记录状态
     */
    private String state;
    /**
     * 交易确认日期
     */
    private String affirmDate;
    /**
     * 交易确认时间
     */
    private String affirmTime;
}
