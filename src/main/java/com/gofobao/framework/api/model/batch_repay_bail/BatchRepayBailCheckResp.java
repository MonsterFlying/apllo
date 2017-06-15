package com.gofobao.framework.api.model.batch_repay_bail;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class BatchRepayBailCheckResp extends JixinBaseResponse{
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 收到的实际金额汇总（请求的txAmount汇总）
     */
    private String txAmount;
    /**
     * 收到的实际笔数
     */
    private String txCounts;
    /**
     *
     */
    private String acqRes;

}
