package com.gofobao.framework.api.model.fund_trans_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class FundTransQueryRequest extends JixinBaseRequest {
    /** 电子账号 */
    private String accountId ;
    /** 原交易日期*/
    private String orgTxDate ;
    /** 原交易流水号 */
    private String orgTxTime ;
    /** 原交易流水号 */
    private String orgSeqNo ;
    /** 原请求方保留 */
    private String acqRes ;
}
