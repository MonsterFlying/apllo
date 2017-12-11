package com.gofobao.framework.api.model.contract_get_contracts;

import com.gofobao.framework.api.request.ContractBaseRequest;
import lombok.Data;

/**
 * Created by master on 2017/11/15.
 */
@Data
public class ContractIdsRequest extends ContractBaseRequest {

    private String accountId;

    private String forAccountId;

    private String contractType;

    private String productId;

    private String userIp;

    private String acqRes;

}
