package com.gofobao.framework.api.model.credit_auth_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/6/14.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditAuthQueryRequest extends JixinBaseRequest {
    private String type;
    private String accountId ;
}
