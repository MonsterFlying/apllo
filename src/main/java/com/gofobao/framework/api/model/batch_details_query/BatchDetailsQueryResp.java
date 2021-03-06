package com.gofobao.framework.api.model.batch_details_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/14.
 */
@Data
public class BatchDetailsQueryResp extends JixinBaseResponse {
    /**
     * 批次交易日期
     */
    private String batchTxDate;
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 交易种类
     * 0-所有交易(包括1，2，不包括9 )
     1-成功交易
     2-失败交易
     9-合法性校验失败交易
     */
    private String type;
    /**
     *
     */
    private String batchTxCode;
    /**
     *
     */
    private String pageNum;
    /**
     *
     */
    private String pageSize;
    /**
     * 总记录数
     */
    private String totalItems;
    /**
     * 结果数组
     */
    private String subPacks;
}
