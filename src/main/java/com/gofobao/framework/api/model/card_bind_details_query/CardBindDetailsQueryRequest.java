package com.gofobao.framework.api.model.card_bind_details_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by admin on 2017/5/17.
 */
@Data
public class CardBindDetailsQueryRequest extends JixinBaseRequest {

    /**
     * 电子账号
     */
    private String accountId;

    private String state = "1" ;
}
