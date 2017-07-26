package com.gofobao.framework.api.model.card_bind_details_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * 初始化密码响应
 * Created by Max on 17/5/23.
 */
@Data
public class CardBindDetailsQueryResponse extends JixinBaseResponse {
    private String accountId ;
    private String name ;
    private String totalItems ;
    private String subPacks ;
}
