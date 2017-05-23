package com.gofobao.framework.api.model.password_set;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * 初始化密码响应
 * Created by Max on 17/5/23.
 */
@Data
public class PasswordSetResponse extends JixinBaseResponse {
    private String accountid ;
    private String acqRes ;
}
