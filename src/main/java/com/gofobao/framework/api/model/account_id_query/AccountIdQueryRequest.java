package com.gofobao.framework.api.model.account_id_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class AccountIdQueryRequest extends JixinBaseRequest {

    private String idType = "01";

    private String idNo;
}
