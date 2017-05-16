package com.gofobao.framework.api.request;

import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public abstract class AbsRequest {
    /**
     * 版本号
     */
    protected String version;
    /**
     * 交易代码
     */
    protected String txCode;
    /**
     * 机构代码
     */
    protected String instCode;
    /**
     * 银行代码
     */
    protected String bankCode;
    /**
     * 交易日期
     */
    protected int txDate;
    /**
     * 交易时间
     */
    protected int txTime;
    /**
     * 交易时间
     */
    protected String seqNo;
    /**
     * 交易流水号
     */
    protected int channel;
}
