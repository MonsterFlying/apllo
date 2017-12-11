package com.gofobao.framework.api.model.contract_details;

import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import lombok.Data;

/**
 *
 * @author master
 * @date 2017/11/15
 */
@Data
public class ContractDetailsResponse extends ContractBaseResponse {

    private String documentDir;

    private String accountId;

    private String acqRes;


}
