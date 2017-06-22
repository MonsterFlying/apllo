package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.tender.contants.AutoTenderContants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Created by Zeke on 2017/6/20.
 */
@ApiModel
@Data
public class VoSaveAutoTenderReq {

    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty(name = "id", value = "自动投标记录id （更新时必填）", dataType = "int", required = false)
    private Long id;

    @ApiModelProperty(name = "status", value = "false禁用 true启用", dataType = "int", required = true)
    @NotNull(message = "自动投标不能为空!")
    private Boolean status;

    @ApiModelProperty(name = "lowest", value = "最低投标金额 分 不填写默认50", required = false)
    private Double lowest = 50 * 100d;

    @ApiModelProperty(name = "borrowTypes", value = "投标种类（0：车贷标；4、渠道标；1、净值标；3、转让标） 选中则带上相应数字 用,隔开 0,1,3", dataType = "String", required = true)
    @NotNull(message = "投标种类不能为空!")
    @Pattern(regexp = "^\\d(,\\d){0,3}$", message = "投标种类填写不符合规则!")
    private transient String borrowTypes;

    @ApiModelProperty(name = "mode", value = "投标方式 0、余额； 1、固定金额；", dataType = "int", hidden = true)
    private Integer mode;

    @ApiModelProperty(name = "timelimitType", value = "期限类型（0、不限定，1、按月，2、按天）", dataType = "int", required = true)
    @NotNull(message = "期限类型不能为空!")
    private Integer timelimitType;

    @ApiModelProperty(name = "timelimitFirst", value = "期限范围起始值 timelimitType不为0时必填", dataType = "int", required = false)
    private Integer timelimitFirst = 0;

    @ApiModelProperty(name = "timelimitLast", value = "期限范围结束值 timelimitType不为0时必填", dataType = "int", required = false)
    private Integer timelimitLast = 0;

    @ApiModelProperty(name = "tenderMoney", value = "最大投标金额(分) ", required = false)
    private Double tenderMoney = null;

    @ApiModelProperty(name = "repayFashions", value = "返款方式（0、按月分期 1、一次性还本付息2、先息后本） 选中则带上相应数字 用,隔开 例如 0,1", dataType = "String", required = true)
    @NotNull(message = "返款方式不能为空!")
    @Pattern(regexp = "^\\d(,\\d){0,2}$", message = "返款方式填写不符合规则!")
    private transient String repayFashions;

    @ApiModelProperty(name = "aprFirst", value = "年化率起始值 14.5% --> 1450", dataType = "int", required = true)
    @NotNull(message = "年化利率起始值不能为空!")
    @Min(value = AutoTenderContants.APR_FIRST, message = "年化利率起始值不能小于8%!")
    private Integer aprFirst;

    @ApiModelProperty(name = "aprLast", value = "年化率结束值 14.5% --> 1450", dataType = "int", required = true)
    @NotNull(message = "年化利率结束值不能为空!")
    @Max(value = AutoTenderContants.APR_LAST, message = "年化利率起始值不能大于24%!")
    private Integer aprLast;

    @ApiModelProperty(name = "saveMoney", value = "账户保留金额（分） 不填写默认0", required = false)
    private Double saveMoney = 0d;

    public Double getLowest() {
        return MathHelper.myRound(lowest, 0);
    }

    public void setLowest(Double lowest) {
        this.lowest = MathHelper.myRound(lowest * 100.0, 0);
    }

    public Double getTenderMoney() {
        return MathHelper.myRound(tenderMoney, 0);
    }

    public void setTenderMoney(Double tenderMoney) {
        this.tenderMoney = MathHelper.myRound(tenderMoney * 100.0, 0);
    }

    public Double getSaveMoney() {
        return MathHelper.myRound(saveMoney, 0);
    }

    public void setSaveMoney(Double saveMoney) {
        this.saveMoney = MathHelper.myRound(saveMoney * 100.0, 0);
    }
}
