package com.gofobao.framework.api.model.batch_lend_pay;

import com.gofobao.framework.api.repsonse.JixinBatchBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/8.
 */
@Data
public class BatchLendPayResp extends JixinBatchBaseResponse {
    /**
     *
     */
    private String batchNo;
    /**
     *
     */
    private String retNotifyURL;
    /**
     *
     */
    private String subPacks;
    /**
     *
     */
    private String txAmount;
    /**
     *
     */
    private String txCounts;
    /**
     *
     */
    private String notifyURL;
    /**
     *
     */
    private String acqRes;
}
