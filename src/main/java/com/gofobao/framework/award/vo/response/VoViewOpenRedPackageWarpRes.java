package com.gofobao.framework.award.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/8.
 */
@Data
public class VoViewOpenRedPackageWarpRes extends VoBaseResp{
    @ApiModelProperty("红包金额")
    private Double money;
}
