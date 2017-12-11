package com.gofobao.framework.api.model.contract_entrust_protocol;

import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import lombok.Data;

/**
 * Created by master on 2017/11/14.
 */
@Data
public class EntrustProtocolResponse extends ContractBaseResponse {

    private String accountId;

    private String name;

    private String contractAuthType;

    private String templateId;

    private String contractId;

    private String contractUrl;

    private String acqRes;

}
