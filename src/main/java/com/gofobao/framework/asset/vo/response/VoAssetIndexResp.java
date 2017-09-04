package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Data
@ApiModel
public class VoAssetIndexResp extends VoBaseResp{
    @ApiModelProperty("累计收益")
    private String accruedMoney ;

    @ApiModelProperty("待收总额")
    private String collectionMoney ;

    @ApiModelProperty("账户余额")
    private String accountMoney;

    @ApiModelProperty("净资产")
    private String totalAsset ;

    @ApiModelProperty("净值额度")
    private String NetAmount ;
}
