package com.gofobao.framework.api.model.offline_recharge_call;

import lombok.Data;

/**
 * 线下充值回调
 * Created by Zeke on 2017/6/23.
 */
@Data
public class OfflineRechargeCallbackResponse {
    private String bankCode;
    private String note;
    private String payAccountId;
    private String seqNo;
    private String txTime;
    private String channel;
    private String sign;
    private String version;
    private String noticeAddress;
    private String txAmount;
    private String orgTxDate;
    private String accountId;
    private String orgSeqNo;
    private String orgTxTime;
    private String name;
    private String instCode;
    private String txCode;
    private String productOwner;
    private String acqRes;
    private String txstsFlag;
    private String txDate;
}
