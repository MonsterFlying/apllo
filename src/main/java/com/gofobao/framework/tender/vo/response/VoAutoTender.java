package com.gofobao.framework.tender.vo.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by Max on 2017/3/27.
 */
@ApiModel
@Data
public class VoAutoTender {
    @ApiModelProperty("自动投标id")
    private Long id;
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Date autoAt;
    @ApiModelProperty("序号")
    private Integer order;
    @ApiModelProperty("排队天数")
    private Integer queueDays;
    @ApiModelProperty("启用状态：false、禁用；true、启用")
    private Boolean status;

}
