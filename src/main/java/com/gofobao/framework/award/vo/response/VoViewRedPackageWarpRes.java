package com.gofobao.framework.award.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Data
@ApiModel
public class VoViewRedPackageWarpRes extends VoBaseResp {

    @ApiModelProperty("红包列表")
    List<RedPackageRes> resList= Lists.newArrayList();
}
