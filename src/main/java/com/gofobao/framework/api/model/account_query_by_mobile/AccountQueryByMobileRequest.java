package com.gofobao.framework.api.model.account_query_by_mobile;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class AccountQueryByMobileRequest extends JixinBaseRequest {
    /**
     * 手机号
     */
    private String mobile;
}
