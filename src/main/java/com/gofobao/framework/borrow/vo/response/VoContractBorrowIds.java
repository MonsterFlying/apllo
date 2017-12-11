package com.gofobao.framework.borrow.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VoContractBorrowIds extends VoBaseResp {

    @ApiModelProperty("标的名")
    private String borrowName;


    @ApiModelProperty("起息时间")
    private String recheckAt;

    @ApiModelProperty("标id")
    private Long borrowId;

}
