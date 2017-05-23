package com.gofobao.framework.api.model.password_reset;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * 重置密码响应
 * Created by Max on 17/5/23.
 */
@Data
public class PasswordResetResponse extends JixinBaseResponse {
    private String accountid ;
    private String acqRes ;
}
