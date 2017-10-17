package com.gofobao.framework.finance.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zeke on 2017/10/17.
 */
@Data
@ApiModel
public class VoViewFinanceServerPlanResp extends VoBaseResp {
    @ApiModelProperty("金服理财计划集合")
    List<FinanceServerPlan> financeServerPlanList = new ArrayList<>();
}
