package com.gofobao.framework.api.model.card_unbind;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * 初始化密码响应
 * Created by Max on 17/5/23.
 */
@Data
public class CardUnbindResponse extends JixinBaseResponse {
    private String accountid ;
    private String acqRes ;
}
