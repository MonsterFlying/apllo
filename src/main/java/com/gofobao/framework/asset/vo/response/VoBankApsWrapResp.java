package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/13 0013.
 */
@Data
@ApiModel

public class VoBankApsWrapResp extends VoBaseResp {
    @ApiModelProperty("当前页数")
    private String page ;

    @ApiModelProperty("总页数")
    private String totalpage ;

    @ApiModelProperty("列表数据")
    private List<VoBankApsResp> record = new ArrayList<>() ;

}
