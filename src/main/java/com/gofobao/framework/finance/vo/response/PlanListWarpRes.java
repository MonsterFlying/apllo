package com.gofobao.framework.finance.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/14.
 */
@Data
public class PlanListWarpRes extends VoBaseResp {
    private List<PlanList> planLists = Lists.newArrayList();

    @ApiModelProperty("总条数")
    private Integer totalCount;

}
