package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Data
@ApiModel("累计详情")
public class VoQueryInfoResp extends VoBaseResp {
    @ApiModelProperty("总金额")
    private String totalMoey;
    @ApiModelProperty("可用金额")
    private String validateMoney;
}
