package com.gofobao.framework.api.model.mobile_modify_plus;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class MobileModifyRequest extends JixinBaseRequest {
    private String accountId ;
    private String option = "1";
    private String mobile ;
    private String lastSrvAuthCode ;
    private String smsCode;
    private String acqRes ;
}
