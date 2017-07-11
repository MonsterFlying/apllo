package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Data
@ApiModel("账户余额")
public class VoAvailableAssetInfoResp extends VoBaseResp {
    @ApiModelProperty("可用余额")
    private Long useMoney;
    @ApiModelProperty("显示可用余额")
    private String viewUseMoney;

    @ApiModelProperty("冻结金额")
    private Long noUseMoney;
    @ApiModelProperty("显示冻结金额")
    private String viewNoUseMoney;

    @ApiModelProperty("总额")
    private Long total;
    @ApiModelProperty("显示总额")
    private String viwTotal;
}
