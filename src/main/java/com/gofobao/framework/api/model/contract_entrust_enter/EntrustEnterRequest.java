package com.gofobao.framework.api.model.contract_entrust_enter;


import com.gofobao.framework.api.request.ContractBaseRequest;
import lombok.Data;

@Data
public class EntrustEnterRequest extends ContractBaseRequest {

    private String accountId;

    private String name;

    private String mobile;

    private String smsCode;

    private String srvAuthCode;

    private String contractId;

    private String contractAuthType;

    private String acpRes;

}
