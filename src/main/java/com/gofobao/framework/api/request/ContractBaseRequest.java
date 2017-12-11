package com.gofobao.framework.api.request;

import lombok.Data;

/**
 * Created by master on 2017/11/16.
 */
@Data
public  class ContractBaseRequest {

    private String txCode;

    private String instCode;

    private String txDate;

    private String txTime;

    private String seqNo;

    private String channel;

}
