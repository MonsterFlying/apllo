package com.gofobao.framework.api.model.contract_sms;

import com.gofobao.framework.api.request.ContractBaseRequest;
import lombok.Data;

/**
 * 发送短信验证码
 * Created by master on 2017/11/14.
 */
@Data
public class SendSmsRequest extends ContractBaseRequest {

    private String mobile;

    private String srvTxCode;

    private String acqRes;

}
