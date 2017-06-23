package com.gofobao.framework.lend.vo.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 2017/3/31.
 */
@ApiModel
@Data
public class VoPickLend {
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long id;
    @ApiModelProperty("借款人id")
    private Long userId;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("借款金额")
    private Integer money;
    @ApiModelProperty("年利率 ")
    private Integer apr;
    @ApiModelProperty("期限")
    private Integer timeLimit;
    @ApiModelProperty("应还时间")
    private String expectRepayAt;
    @ApiModelProperty("是否拉黑")
    private Boolean isBlacklist;
    @ApiModelProperty("实际还时间")
    private String realityRepayAt;

}
