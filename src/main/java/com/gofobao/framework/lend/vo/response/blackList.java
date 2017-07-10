package com.gofobao.framework.lend.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/8.
 */
@Data
public class blackList {

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("添加时间")
    private String addTime;

}
