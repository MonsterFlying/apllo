package com.gofobao.framework.api.model.freeze_details_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/9/15.
 */
@Data
public class FreezeDetailsQueryRequest extends JixinBaseRequest{
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 0-所有冻结
     1-有效冻结（尚未解冻）
     */
    private String state;
    /**
     * 起始日期
     */
    private String startDate;
    /**
     * 结束日期
     */
    private String endDate;
    /**
     * 页数
     */
    private String pageNum;
    /**
     * 页长
     */
    private String pageSize;
}
