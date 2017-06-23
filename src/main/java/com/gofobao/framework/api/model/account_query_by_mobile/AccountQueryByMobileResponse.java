package com.gofobao.framework.api.model.account_query_by_mobile;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class AccountQueryByMobileResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 证件类型
     */
    private String idType;
    /**
     *证件号码
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
     * 账户状态 空-正常
     A-待激活
     C-止付
     Z-注销
     */
    private String acctState;
}
