package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@Data
@ApiModel("还款详情")
public class VoViewRepaymentOrderDetailWarpRes extends VoBaseResp {
    private RepaymentOrderDetail repaymentOrderDetail;

}
