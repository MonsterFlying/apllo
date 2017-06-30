package com.gofobao.framework.award.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/7.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RedPackageRes {
    @ApiModelProperty("红包金额 查询未领取和已过期红包时 请忽略该字段")
    private String money;
    @ApiModelProperty("红包标题")
    private String title;
    @ApiModelProperty("有效期")
    private String expiryDate;
    @ApiModelProperty("1：注册；2：新手标 ；3：老用户投资送红包 4：邀请好友投资送红包 ,")
    private Integer type;
    @ApiModelProperty("红包id 查询未领取和已过期红包时 请忽略该字段")
    private Integer redPackageId;
}
