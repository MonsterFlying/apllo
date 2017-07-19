package com.gofobao.framework.api.model.batch_credit_end;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/19.
 */
@Data
public class BatchCreditEndResp extends JixinBaseResponse{
    /**
     *  success接收成功
     */
    private String received;
}
