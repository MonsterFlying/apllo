package com.gofobao.framework.api.repsonse;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/8.
 */
@Data
public class JixinBatchBaseResponse extends JixinBaseResponse{
    /**
     * 接收结果  success接收成功
     */
    private String  received;
}
