package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/20 0020.
 */
@Data
@ApiModel
public class VoBankListResp extends VoBaseResp{
    @ApiModelProperty("银行卡logo")
    private String bankLogo;

    @ApiModelProperty("银行卡名称")
    private String bankName ;

    @ApiModelProperty("银行卡类型")
    private String bankType ;

    @ApiModelProperty("银行卡名称")
    private String bankCard ;
}
