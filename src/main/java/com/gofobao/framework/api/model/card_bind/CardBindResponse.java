package com.gofobao.framework.api.model.card_bind;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * 初始化密码响应
 * Created by Max on 17/5/23.
 */
@Data
public class CardBindResponse extends JixinBaseResponse {
    private String accountid ;
    private String acqRes ;
}
