package com.gofobao.framework.comment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by xin on 2017/11/8.
 */
@Data
@ApiModel("主题")
public class VoTopicResp extends VoBaseResp {
    @ApiModelProperty("主题标题")
    private String title;

    @ApiModelProperty("发帖用户")
    private String userName;

    @ApiModelProperty("用户图像标识")
    private String userIconUrl;

    @ApiModelProperty("是否置顶")
    private Integer fixState;

    @ApiModelProperty("最热标识")
    private Integer hotState;

    @ApiModelProperty("最新标识")
    private Integer newState;

    @ApiModelProperty("点赞总数")
    private Integer topTotalNum;

    @ApiModelProperty("评论总数")
    private Integer contentTotalNum;

    @ApiModelProperty("记录有效状态")
    private Integer del;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("更新时间")
    private Date updateDate;

    @ApiModelProperty("图片1")
    private String img1;

    @ApiModelProperty("图片2")
    private String img2;

    @ApiModelProperty("图片3")
    private String img3;

    @ApiModelProperty("图片4")
    private String img4;

    @ApiModelProperty("图片5")
    private String img5;

    @ApiModelProperty("图片6")
    private String img6;

    @ApiModelProperty("图片7")
    private String img7;

    @ApiModelProperty("图片8")
    private String img8;

    @ApiModelProperty("图片9")
    private String img9;

    @ApiModelProperty("主题内容")
    private String content;
}
