package com.gofobao.framework.api.model.password_set_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

@Data
public class PasswordSetQueryResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId ;
    /**
     * 是否设置过密码
     */
    private String pinFlag ;
}
