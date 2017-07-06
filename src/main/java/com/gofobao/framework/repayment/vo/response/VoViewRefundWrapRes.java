package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/5.
 */
@Data
@ApiModel
public class VoViewRefundWrapRes extends VoBaseResp{
    @ApiModelProperty("还款中列表")
    private List<VoViewRefundRes> list =  Lists.newArrayList() ;
    @ApiModelProperty("总记录数")
    private Integer totalCount=0;
}
