package com.gofobao.framework.api.model.contract_entrust_enter;

import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import lombok.Data;

@Data
public class EntrustEnterResponse extends ContractBaseResponse {

    private String accountId;

    private String contractId;

    private String acpRes;

}
