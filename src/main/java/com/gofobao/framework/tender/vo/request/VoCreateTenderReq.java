package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import com.gofobao.framework.helper.MathHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/5/31.
 */
@ApiModel
@Data
public class VoCreateTenderReq extends VoBaseReq {
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty(name = "tenderMoney", value = "投标金额", dataType = "double",required = true)
    @NotNull(message = "投标金额不能为空!")
    private Double tenderMoney;


    @ApiModelProperty(name = "payPassword", value = "交易密码", dataType = "String", required = false)
    private String payPassword;

    @ApiModelProperty(name = "borrowId", value = "借款ID", dataType = "int", required = true)
    private Long borrowId;

    @ApiModelProperty(name = "borrowPassword", value = "借款密码", dataType = "String", required = false)
    private String borrowPassword;

    @ApiModelProperty(name = "tenderSource", value = "投标来源：1：android；2：ios 3：H5 ", hidden = true)
    private Integer tenderSource;

    @ApiModelProperty(name = "autoOrder", value = "自动排序 ", hidden = true)
    private Integer autoOrder;

    @ApiModelProperty(name = "isAutoTender", value = "是否自动投标", hidden = true)
    private Boolean isAutoTender = false;

    @ApiModelProperty(hidden = true)
    private Integer source;

    @ApiModelProperty(hidden = true)
    private String requestSource;


    public Double getTenderMoney() {
        return MathHelper.myRound(tenderMoney, 0);
    }

    public void setTenderMoney(Double tenderMoney) {
        this.tenderMoney = MathHelper.myRound(tenderMoney * 100.0, 0);
    }


}
