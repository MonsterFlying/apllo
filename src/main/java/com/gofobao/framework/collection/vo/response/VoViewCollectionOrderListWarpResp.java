package com.gofobao.framework.collection.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/5/31.
 */
@Data
@ApiModel
public class VoViewCollectionOrderListWarpResp extends VoBaseResp{

    @ApiModelProperty("总笔数")
    private Integer order;

    @ApiModelProperty("回款总金额")
    private String sumCollectionMoneyYes;

    @ApiModelProperty("回款列表")
    private List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();

}
