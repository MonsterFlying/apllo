package com.gofobao.framework.api.model.batch_bail_repay;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class BatchBailRepayResp extends JixinBaseResponse{
    /**
     *  success接收成功
     */
    private String received;
}
