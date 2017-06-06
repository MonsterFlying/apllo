package com.gofobao.framework.api.model.debt_details_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class DebtDetailsQueryReq extends JixinBaseRequest{
    /**
     * 存管平台分配的借款人电子账号
     */
    private String accountId;
    /**
     * 标的id borrowId  为空表示查询所有标的productId不能与（startDate和endDate）同时为空 选填
     */
    private String productId;
    /**
     * YYYYMMDD，对应募集日 选填
     */
    private String startDate;
    /**
     * YYYYMMDD，对应募集日 选填
     */
    private String endDate;
    /**
     * 查询页数 起始 1
     */
    private String pageNum;
    /**
     * 每页笔数
     */
    private String pageSize;
}
