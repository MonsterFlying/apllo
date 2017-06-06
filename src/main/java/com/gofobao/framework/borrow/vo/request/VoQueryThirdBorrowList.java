package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class VoQueryThirdBorrowList {
    @ApiModelProperty(value = "用户id", dataType = "int")
    private Long userId;

    @ApiModelProperty(value = "标的id", dataType = "String")
    private Long borrowId;
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
