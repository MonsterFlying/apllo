package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@ApiModel
@Data
public class VoCollectionResp extends VoBaseResp{
    @ApiModelProperty("待收利息")
    private String interest;
    @ApiModelProperty("不显示待收利息")
    private Integer hideInterest;

    @ApiModelProperty("待收本金")
    private String principal;
    @ApiModelProperty("不显示待收本金")
    private Integer hidePrincipal;

    @ApiModelProperty("待收总金额")
    private String waitCollectionTotal;
    @ApiModelProperty("不显示待收总金额")
    private Integer hideWaitCollectionTotal;
}
