package com.gofobao.framework.api.model.contract_sms;

import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import lombok.Data;

/**
 * Created by master on 2017/11/14.
 */
@Data
public class SendSmsResponse extends ContractBaseResponse {

    private String mobile;

    private String srvTxCode;

    private String srvAuthCode;

    private String sendTime;

    private String acqRes;

}
