package com.gofobao.framework.api.model.account_id_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class AccountIdQueryResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 证件类型
     */
    private String idType;
    /**
     * 证件号码
     */
    private String idNo;

    /**
     * 开户日期
     */
    private String OpenDate ;

    /**
     *  账户状态
     */
    private String acctState ;

    /**
     *  冻结状态
     */
    private String frzState ;

    /**
     *  密码挂失状态
     */
    private String pinLosCd ;
}
