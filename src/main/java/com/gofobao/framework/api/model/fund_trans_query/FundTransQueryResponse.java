package com.gofobao.framework.api.model.fund_trans_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class FundTransQueryResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId ;

    /**
     * 姓名
     */
    private String name ;

    /**
     * 交易金额
     */
    private String txAmount ;

    /**
     * 冲正撤销标志
     */
    private String orFlag ;

    /**
     * 交易处理结果
     */
    private String result ;
    /**
     * 请求方保留
     */
    private String acqRes ;
}
