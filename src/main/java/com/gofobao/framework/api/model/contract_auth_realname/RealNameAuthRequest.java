package com.gofobao.framework.api.model.contract_auth_realname;

import com.gofobao.framework.api.request.ContractBaseRequest;
import lombok.Data;

/**
 * Created by master on 2017/11/14.
 */
@Data
public class RealNameAuthRequest extends ContractBaseRequest {

    private String accountId;

    private String idType;

    private String idNo;

    private String name;

    private String mobile;

    private String smsCode;

    private String srvAuthCode;

    private String userIP;

    private String acqRes;

}
