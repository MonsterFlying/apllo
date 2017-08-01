package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.helper.MathHelper;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/7/31.
 */
@Data
public class VoBuyTransfer {
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;
    @ApiModelProperty(value = "债权转让记录id")
    @NotNull(message = "债权转让记录id不能为空!")
    private Long transferId;
    @ApiModelProperty(name = "buyMoney", value = "购买债权金额", dataType = "double", required = true)
    @NotNull(message = "购买债权金额不能为空!")
    private Double buyMoney;
    @ApiModelProperty(hidden = true)
    private Boolean auto = false;
    @ApiModelProperty(hidden = true)
    private Integer autoOrder = 0;

    public Double getBuyMoney() {
        return MathHelper.myRound(buyMoney, 0);
    }

    public void setBuyMoney(Double buyMoney) {
        this.buyMoney = MathHelper.myRound(buyMoney * 100.0, 0);
    }
}
