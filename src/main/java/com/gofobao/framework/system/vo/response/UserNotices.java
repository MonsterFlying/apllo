package com.gofobao.framework.system.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/15.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserNotices {
    private Long id;

    @ApiModelProperty("创建时间")
    private String time;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("是否阅读（0、未读；1、已读）")
    private Boolean stauts;

    @ApiModelProperty(hidden = true)
    private Integer totalCount;
}
