package com.gofobao.framework.api.model.sms_code_apply;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Max on 17/5/19.
 */
@Data
public class SmsCodeApplyResponse extends JixinBaseResponse {
    /** 手机*/
    private String mobile ;
    /** 业务交易代码*/
    private String srvTxCode ;
    /** 业务权限码*/
    private String srvAuthCode ;
    /** 请求方保留*/
    private String acqRes ;
}
