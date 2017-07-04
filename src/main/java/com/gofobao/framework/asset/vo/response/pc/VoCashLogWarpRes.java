package com.gofobao.framework.asset.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/3.
 */
@Data
public class VoCashLogWarpRes extends VoBaseResp{

    private List<VoCashLog> logs= Lists.newArrayList();

    @ApiModelProperty("总记录数")
    private Integer totalCount;
}
