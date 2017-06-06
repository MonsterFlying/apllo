package com.gofobao.framework.asset.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/22.
 */
@Data
@ApiModel("资金")
public class VoViewAssetLogRes {
        @ApiModelProperty("红包类型")
        private  String type;
        @ApiModelProperty("创建时间")
        private String  createdAt;
        @ApiModelProperty("金额")
        private String money;
}
