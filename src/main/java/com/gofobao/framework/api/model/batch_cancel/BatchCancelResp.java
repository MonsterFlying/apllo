package com.gofobao.framework.api.model.batch_cancel;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/27.
 */
@Data
public class BatchCancelResp extends JixinBaseResponse{
    /**
     * 需要撤销的批次号
     */
    private String batchNo;
    /**
     * 批次号对应的交易代码
     */
    private String batchTxCode;
    /**
     * 
     */
    private String acqRes;
}
