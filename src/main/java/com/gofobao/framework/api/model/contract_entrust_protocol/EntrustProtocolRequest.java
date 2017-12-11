package com.gofobao.framework.api.model.contract_entrust_protocol;

import com.gofobao.framework.api.request.ContractBaseRequest;
import lombok.Data;

/**
 * Created by master on 2017/11/14.
 *
 */
@Data
public class EntrustProtocolRequest extends ContractBaseRequest {

    private String accountId;

    private String name;

    private String idType;

    private String idNo;

    private String mobile;

    private String contractAuthType;

    private Integer templateId;

    private String subPacks;

    private String userIP;

    private String acqRes;
}
