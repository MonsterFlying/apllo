package com.gofobao.framework.tender.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 17/4/1.
 */
@ApiModel("自动投标详情")
@Data
public class VoAutoTenderInfo extends VoBaseResp {
    @ApiModelProperty(name = "id", value = "自动投标记录id （更新时必填）", dataType = "int", required = false)
    private Long id;

    @ApiModelProperty(name = "status", value = "false禁用 true启用", dataType = "int", required = true)
    private Boolean status;

    @ApiModelProperty(name = "buyMoney", value = "最低投标金额 分 不填写默认0", dataType = "int", required = false)
    private String lowest = "0";
    @ApiModelProperty(name = "showLowest", value = "最低投标金额 分 不填写默认0", dataType = "int", required = false)
    private String showLowest = "0";

    @ApiModelProperty(name = "borrowTypes", value = "投标种类（0：车贷标；4、渠道标；1、净值标；3、转让标） 选中则带上相应数字 用,隔开 0,1,3", dataType = "String", required = true)
    private String borrowTypes;

    @ApiModelProperty(name = "mode", value = "投标方式 0、余额； 1、固定金额；", dataType = "int", required = true)
    private Integer mode;

    @ApiModelProperty(name = "timelimitType", value = "期限类型（0、不限定，1、按月，2、按天）", dataType = "int", required = true)
    private Integer timelimitType;

    @ApiModelProperty(name = "timelimitFirst", value = "期限范围起始值 timelimitType不为0时必填", dataType = "int", required = false)
    private Integer timelimitFirst = 0;

    @ApiModelProperty(name = "timelimitLast", value = "期限范围结束值 timelimitType不为0时必填", dataType = "int", required = false)
    private Integer timelimitLast = 0;

    @ApiModelProperty(name = "tenderMoney", value = "最大投标金额(分)  mode = 1 必须填写", dataType = "int", required = false)
    private String tenderMoney = "0";
    @ApiModelProperty(name = "showTenderMoney", value = "最大投标金额(分)  mode = 1 必须填写", dataType = "int", required = false)
    private String showTenderMoney = "0";

    @ApiModelProperty(name = "repayFashions", value = "返款方式（0、按月分期 2、先息后本 1、一次性还本付息） 选中则带上相应数字 用,隔开 例如 0,1", dataType = "String", required = true)
    private String repayFashions;

    @ApiModelProperty(name = "aprFirst", value = "年化率起始值 14.5% --> 1450", dataType = "int", required = true)
    private Integer aprFirst;

    @ApiModelProperty(name = "aprLast", value = "年化率结束值 14.5% --> 1450", dataType = "int", required = true)
    private Integer aprLast;

    @ApiModelProperty(name = "saveMoney", value = "账户保留金额（分） 不填写默认0", dataType = "int", required = false)
    private String saveMoney = "0";

    @ApiModelProperty(name = "showSaveMoney", value = "账户保留金额（分） 不填写默认0", dataType = "int", required = false)
    private String showSaveMoney = "0";

}
