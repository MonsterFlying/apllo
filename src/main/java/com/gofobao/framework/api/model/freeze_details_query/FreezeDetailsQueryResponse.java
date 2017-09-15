package com.gofobao.framework.api.model.freeze_details_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/9/15.
 */
@Data
public class FreezeDetailsQueryResponse extends JixinBaseResponse{
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 查询记录状态
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
     * 姓名
     */
    private String name;
    /**
     * 页数
     */
    private String pageNum;
    /**
     * 页长
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
