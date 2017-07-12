package com.gofobao.framework.borrow.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gofobao.framework.helper.MathHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static com.gofobao.framework.borrow.contants.BorrowVerifyContants.*;

/**
 * Created by Zeke on 2017/5/26.
 */
@Data
@ApiModel
public class VoAddNetWorthBorrow {

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long userId;

    /**
     * 借款期限
     */
    @ApiModelProperty(name = "timeLimit", value = "借款期限", dataType = "int", required = true)
    @NotNull(message = "借款期限不能为空!")
    @Min(value = DAY_MIN, message = "借款天数不小于7天!")
    @Max(value = DAY_MAX, message = "借款天数不能大于92天!")
    private int timeLimit;

    /**
     * 借款金额
     */
    @ApiModelProperty(name = "money", value = "借款金额(元)", dataType = "double", required = true)
    @NotNull(message = "借款金额不能为空!")
    @Min(value = MONEY_MIN, message = "借款金额参数有误!")
    @Max(value = MONEY_MAX, message = "借款金额参数有误!")
    private double money;

    /**
     * 年化率
     */
    @ApiModelProperty(name = "apr", value = "年化率", dataType = "int", required = true)
    @NotNull(message = "年化率不能为空!")
    @Min(value = APR_MIN, message = "年华利率参数有误!")
    @Max(value = APR_MAX, message = "年华利率参数有误!")
    private int apr;

    /**
     * 招标时间
     */
    @ApiModelProperty(name = "validDay", value = "有效招标时间", dataType = "int", required = true)
    @NotNull(message = "有效招标时间不能为空!")
    @Min(value = 1, message = "有效招标时间参数有误!")
    private int validDay;

    /**
     * 借款标题
     */
    @ApiModelProperty(name = "name", value = "借款标题", dataType = "String", required = true)
    @NotNull(message = "借款标题不能为空!")
    private String name;

    /**
     * 布发时间
     */
    @ApiModelProperty(name = "releaseAt", value = "布发时间", dataType = "String", required = true, example = "2017-03-07 19:35:00")
    @NotNull(message = "布发时间不能为空!")
    private String releaseAt;

    /**
     * 关闭自动投标
     */
    @ApiModelProperty(name = "closeAuto", value = "关闭自动投标", dataType = "String", required = true, example = "0：不关闭，1：关闭")
    @NotNull(message = "关闭自动投标不能为空!")
    private boolean closeAuto;

    public double getMoney() {
        return (int) MathHelper.myRound(money, 0);
    }

    public void setMoney(double money) {
        this.money = money * 100.0;
    }
}
