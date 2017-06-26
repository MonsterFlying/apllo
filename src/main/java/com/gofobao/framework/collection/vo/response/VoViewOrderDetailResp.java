package com.gofobao.framework.collection.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@Data
@ApiModel("回款详情")
public class VoViewOrderDetailResp extends VoBaseResp {
    @ApiModelProperty("第几期")
    private Integer order;

    @ApiModelProperty("回款日期")
    private String startAt;

    @ApiModelProperty("逾期天数")
    private Integer lateDays;

    @ApiModelProperty("应收本金")
    private String collectionMoney;

    @ApiModelProperty("已收本金")
    private String principal;

    @ApiModelProperty("已收利息")
    private String interest;

    @ApiModelProperty("项目名")
    private String borrowName;

    @ApiModelProperty("状态")
    private String status;


}
