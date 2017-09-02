package com.gofobao.framework.lend.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewLend {
    @ApiModelProperty("名字")
    private String userName;
    @ApiModelProperty("年华率")
    private String apr;
    @ApiModelProperty("期限")
    private Integer limit;
    @ApiModelProperty("金额")
    private String money;
    @ApiModelProperty("出借ID")
    private Long lendId;
    @ApiModelProperty("状态描述")
    private String statusStr;
    @ApiModelProperty("进度")
    private Double spend;
    @ApiModelProperty("状态(0:可借,1：结束)")
    private Integer status;
    @ApiModelProperty("还款时间")
    private String collectionAt;
    @ApiModelProperty("发布时间")
    private String releaseAt;
    @ApiModelProperty("头像")
    private String avatar;
    @ApiModelProperty("起借金额")
    private String startMoney;


}
