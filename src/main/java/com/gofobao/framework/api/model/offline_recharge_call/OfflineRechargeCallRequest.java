package com.gofobao.framework.api.model.offline_recharge_call;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class OfflineRechargeCallRequest extends JixinBaseRequest {
    /** 电子账号*/
    private String accountId ;
    /** 原交易日期*/
    private String orgTxDate ;
    /** 原交易流水号*/
    private String orgTxTime ;

    /** 原交易流水号*/
    private String orgSeqNo ;

    /** 交易金额*/
    private String txAmount ;

    /** 付款账号*/
    private String payAccount ;

    /** 推送地址*/
    private String noticeAddress ;

    /** 姓名*/
    private String name ;

    /** 退汇标志*/
    private String txstsFlag ;

    /** 摘要*/
    private String note ;

    /** */
    private String acqRes ;
}
