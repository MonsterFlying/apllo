package com.gofobao.framework.api.model.debt_details_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class DebtDetailsQueryResp extends JixinBaseResponse{
    /**
     * 存管平台分配的借款人电子账号
     */
    private String accountId;
    /**
     *
     */
    private String startDate;
    /**
     *
     */
    private String endDate;
    /**
     * 姓名
     */
    private String name;
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
