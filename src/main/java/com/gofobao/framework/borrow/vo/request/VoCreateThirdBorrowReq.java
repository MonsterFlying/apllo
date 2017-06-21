package com.gofobao.framework.borrow.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/6/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
public class VoCreateThirdBorrowReq extends VoBaseReq {

    @ApiModelProperty(value = "借款id", dataType = "int")
    private Long borrowId;
    @ApiModelProperty(value = "是否受托支付", hidden = true)
    private Boolean entrustFlag = false;//默认非受托支付
}
