package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/22.
 */
@Data
@ApiModel
public class VoViewFindOrderLogisticsDetailRes extends VoBaseResp {
    @ApiModelProperty("物流状态:0暂不支持")
    private String status;
    @ApiModelProperty("承运来源")
    private String expressName;
    @ApiModelProperty("订单单号")
    private String expressNumber;
    @ApiModelProperty("派送员姓名")
    private String expressmanName;
    @ApiModelProperty("派送员电话")
    private String expressmanPhone;
    @ApiModelProperty("派送节点")
    private List<VoProductLogistics> productLogisticsList;
}
