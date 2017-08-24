package com.gofobao.framework.api.model.password_set_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

@Data
public class PasswordSetQueryRequest extends JixinBaseRequest {
    private String accountId ;
}
