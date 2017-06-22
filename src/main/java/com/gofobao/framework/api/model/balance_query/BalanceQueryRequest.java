package com.gofobao.framework.api.model.balance_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/5/16.`
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceQueryRequest extends JixinBaseRequest {
    private String accountId ;
}
