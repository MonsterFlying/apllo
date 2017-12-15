package com.gofobao.framework.borrow.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/15.
 */
@Data
public class BorrowStatistics {
    @ApiModelProperty("所有项目")
    private Integer sum;
    @ApiModelProperty("车贷")
    private Integer cheDai;
    @ApiModelProperty("信用标")
    private Integer jingZhi;
    @ApiModelProperty("渠道")
    private Integer quDao;
    @ApiModelProperty("流转")
    private Integer liuZhuan;
    @ApiModelProperty("秒标")
    private Integer miaoBiao;

}
