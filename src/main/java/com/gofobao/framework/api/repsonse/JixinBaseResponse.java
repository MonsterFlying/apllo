package com.gofobao.framework.api.repsonse;

import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class JixinBaseResponse {
    /**  版本号 */
    protected String version;
    /**  交易代码 */
    protected String txCode;
    /**  机构代码 */
    protected String instCode;
    /**  银行代码 */
    protected String bankCode;
    /**  交易日期 */
    protected String txDate;
    /**  交易时间  */
    protected String txTime;
    /**  交易流水号 */
    protected String seqNo;
    /**  交易渠道 */
    protected String channel;
    /**  响应代码 */
    protected String retCode;
    /**  响应描述 */
    protected String retMsg;
    protected String sign ;
}
