package com.gofobao.framework.api.model.batch_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/18.
 */
@Data
public class BatchQueryReq extends JixinBaseRequest {
    /**
     * 批次交易日期
     */
    private String batchTxDate;
    /**
     * 批次号
     */
    private String batchNo;
}
