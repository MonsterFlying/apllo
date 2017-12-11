package com.gofobao.framework.api.model.contract_auth_realname;

import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import lombok.Data;

/**
 * Created by master on 2017/11/14.
 */
@Data
public class RealNameAuthResponse extends ContractBaseResponse {


    private String accountId;

    private String name;

    private String acqRes;

}
