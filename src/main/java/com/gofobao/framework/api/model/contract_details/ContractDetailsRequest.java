package com.gofobao.framework.api.model.contract_details;

import com.gofobao.framework.api.request.ContractBaseRequest;
import lombok.Data;

/**
 * @author master
 * @date 2017/11/15
 */
@Data
public class ContractDetailsRequest extends ContractBaseRequest {

    private String accountId;

    private String contractId;

    private Integer documentType;

    private String userIP;

    private String acqRes;


}
