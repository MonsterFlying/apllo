package com.gofobao.framework.borrow.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/5/31.
 */
@Data
public class VoViewCollectionOrderListRes {

    @ApiModelProperty("总笔数")
    private Integer order;

    @ApiModelProperty("回款总金额")
    private String sumCollectionMoneyYes;

    @ApiModelProperty("回款列表")
    private List<VoViewCollectionOrderRes> orderResList;

}
