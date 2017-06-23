package com.gofobao.framework.lend.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Max on 2017/3/31.
 */
@ApiModel
@Data
public class VoViewPickLendList extends VoBaseResp{
    @ApiModelProperty("摘草列表")
    private List<VoPickLend> pickList;
    @ApiModelProperty("本页页码")
    private Integer pageIndex;
    @ApiModelProperty("本页内容数")
    private Integer pageSize;

}
