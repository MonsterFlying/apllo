package com.gofobao.framework.finance.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@Data
@ApiModel("回款详情")
public class VoViewFinanceCollectionDetailResp extends VoBaseResp {
    @ApiModelProperty("第几期")
    private Integer order;

    @ApiModelProperty("回款日期")
    private String collectionAt;

    @ApiModelProperty("应收本息")
    private String collectionMoney;

    @ApiModelProperty("已收本金")
    private String principal;

    @ApiModelProperty("已收利息")
    private String interest;

    @ApiModelProperty("项目名")
    private String borrowName;

    @ApiModelProperty("状态描述")
    private String statusStr;

}
