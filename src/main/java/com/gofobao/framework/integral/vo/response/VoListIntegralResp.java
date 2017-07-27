package com.gofobao.framework.integral.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@ApiModel
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoListIntegralResp extends VoBaseResp{

    /**
     * 总积分
     */
    @ApiModelProperty("总积分")
    private Long totalIntegral;

    /**
     * 有效积分
     */
    @ApiModelProperty("有效积分")
    private Long availableIntegral;

    /**
     * 已使用积分
     */
    @ApiModelProperty("已使用积分")
    private Long invalidIntegral;

    /**
     * 兑换比例
     */
    @ApiModelProperty("兑换比例")
    private String takeRates;

    /**
     * 待收金额
     */
    @ApiModelProperty("待收金额")
    private String  collectionMoney="0";

    /**
     * 积分列表
     */
    @ApiModelProperty("积分列表")
    private List<VoIntegral> voIntegralList;


    @ApiModelProperty("积分兑换规则")
    private String descImage;
}
