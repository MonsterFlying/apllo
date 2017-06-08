package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 17/6/8.
 */
@Data
@ApiModel
public class VoRechargeEntityWrapResp extends VoBaseResp{
    @ApiModelProperty("数据列表")
    List<VoRechargeEntityResp> list = new ArrayList<>() ;
}
