package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/14.
 */
@Data
@ApiModel
public class VoFinanceIndexResp extends VoBaseResp{
    @ApiModelProperty
    private List<IndexBanner> bannerList= Lists.newArrayList();

    @ApiModelProperty
    private NewIndexStatisics newIndexStatisics;

    @ApiModelProperty
    private FinanceIndexBorrow financeIndexBorrow ;
}
