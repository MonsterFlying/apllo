package com.gofobao.framework.api.repsonse;

import lombok.Data;

/**
 * Created by master on 2017/11/16.
 */
@Data
public  class ContractBaseResponse {

    private String txCode;

    private String instCode;

    private String txDate;

    private String txTime;

    private String seqNo;

    private String channel;

    private String result;

    private String message;

    private String sign;



}
