package com.gofobao.framework.api.model.sms_code_apply;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * 短信发送请求
 * Created by Max on 17/5/19.
 */
@Data
public class SmsCodeApplyRequest extends JixinBaseRequest {
    /** 手机号*/
    private String mobile ;
    /** 业务交易代码*/
    private String srvTxCode ;
    /** 请求方保留*/
    private String acqRes ;
}
