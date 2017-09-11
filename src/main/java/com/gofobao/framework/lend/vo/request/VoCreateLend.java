package com.gofobao.framework.lend.vo.request;

import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.lend.contants.LendContants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/6/9.
 */
@Data
public class VoCreateLend {
    @ApiModelProperty(name = "userId", hidden = true)
    public Long userId;

    @ApiModelProperty(name = "money", value = "借款金额 min:1000|max:20000000", required = true)
    @NotNull(message = "借款金额不能为空!")
    @Min(value = LendContants.MIN_MONEY, message = "借款金额不能低于" + LendContants.MIN_MONEY / 100 + "元!")
    @Max(value = LendContants.MAX_MONEY, message = "借款金额不能高于" + LendContants.MAX_MONEY / 100 + "元!")
    private Double money; //借款金额

    @ApiModelProperty(name = "apr", value = "年利率 between:100,2400", dataType = "int", required = true)
    @NotNull(message = "年利率不能为空!")
    @Min(value = LendContants.MIN_APR, message = "年利率不能低于" + LendContants.MIN_APR / 100 + "%!")
    @Max(value = LendContants.MAX_APR, message = "年利率不能高于" + LendContants.MAX_APR / 100 + "%!")
    private Integer apr; //年利率

    @ApiModelProperty(name = "lowest", value = "最低金额(分) min:1000|max:借款金额", required = true)
    @NotNull(message = "最低金额不能为空!")
    @Min(value = LendContants.MIN_LOWEST, message = "最低金额不能低于" + LendContants.MIN_LOWEST / 100 + "元!")
    private Double lowest; //最低金额

    @ApiModelProperty(name = "timeLimit", value = "期限 between:1,92", dataType = "1-92天", required = true)
    @NotNull(message = "期限不能为空!")
    @Min(value = LendContants.MIN_TIME_LIMIT, message = "期限不能低于" + LendContants.MIN_TIME_LIMIT + "天!")
    @Max(value = LendContants.MAX_TIME_LIMIT, message = "期限不能高于" + LendContants.MAX_TIME_LIMIT + "天!")
    private Integer timeLimit; //期限

    @ApiModelProperty(name = "repayAt", value = "还款时间 (yyyy-MM-dd HH:mm:ss)", dataType = "String", required = true)
    @NotNull(message = "还款时间不能为空!")
    private String repayAt; //还款时间

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = MoneyHelper.round(money * 100.0, 0);
    }

    public Double getLowest() {
        return lowest;
    }

    public void setLowest(Double lowest) {
        this.lowest = MoneyHelper.round(lowest * 100.0, 0);
    }
}
