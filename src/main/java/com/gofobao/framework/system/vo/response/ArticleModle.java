package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/7.
 */
@Data
public class ArticleModle {
    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("文章图片")
    private String imgUrl;

    @ApiModelProperty("id")
    private Long id;
}
