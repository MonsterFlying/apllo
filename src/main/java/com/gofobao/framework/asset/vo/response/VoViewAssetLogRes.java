package com.gofobao.framework.asset.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/22.
 */
@Data
@ApiModel("资金")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoViewAssetLogRes {
        @ApiModelProperty("资金变换类型")
        private  String typeName;
        @ApiModelProperty("创建时间")
        private String  createdAt;
        @ApiModelProperty("金额")
        private String money;
        @ApiModelProperty("显示金额")
        private String showMoney;
        @ApiModelProperty("备注")
        private String remark;
        @ApiModelProperty("可用金额")
        private String useMoney;
        @ApiModelProperty("隐藏可用金额")
        private Double hideUseMoney;
        @ApiModelProperty("冻结金额")
        private String noUseMoney;

}
