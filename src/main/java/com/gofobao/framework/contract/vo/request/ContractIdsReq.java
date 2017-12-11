package com.gofobao.framework.contract.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/11/15.
 */
@Data
public class ContractIdsReq {

    @ApiModelProperty("标的号")
    private Long borrowId;

    @ApiModelProperty("id")
    private Long userId;

    @ApiModelProperty("01:开户合同; 02:授权合同;03:借贷合同;04:债转合同")
    private String contractType;

    @ApiModelProperty("对手id")
    private Long forUserId;

    @ApiModelProperty(hidden = true)
    private Long tempUserId;

    @ApiModelProperty(hidden = true)
    private String batchNo;

}
