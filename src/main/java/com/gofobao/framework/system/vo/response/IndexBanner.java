package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/14.
 */
@Data
public class IndexBanner {
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("图片地址")
    private String imageUrl;
    @ApiModelProperty("图片点击")
    private String clickUrl;
}
