package com.gofobao.framework.borrow.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/9.
 */
@Data
public class BorrowInfoRes extends VoBaseResp {

    @ApiModelProperty("每万元收益")
    private String earnings;

    @ApiModelProperty("期限")
    private String timeLimit;

    @ApiModelProperty("还款方式；0：按月分期；1：一次性还本付息；2：先息后本")
    private Integer repayFashion;

    @ApiModelProperty("投标记录")
    private String tenderCount;

    @ApiModelProperty("起投金额")
    private String lowest;

    @ApiModelProperty("融资金额")
    private String money;

    @ApiModelProperty("显示剩余金额")
    private String viewSurplusMoney;

    @ApiModelProperty("不显示剩余金额")
    private Long hideSurplusMoney;

    @ApiModelProperty("进度")
    private String spend;

    @ApiModelProperty("年华率")
    private String apr;

    @ApiModelProperty("结束时间")
    private String endAt;

    @ApiModelProperty("满标时间")
    private String successAt;

    @ApiModelProperty("状态 1.待发布 2.还款中 3.招标中 4.已完成 5.已过期,6.待复审")
    private Integer status;

    @ApiModelProperty("秒差 ：当状态是招标中 为正数  其他状态则返回-1")
    private Long surplusSecond;

    @ApiModelProperty("标类型 type: 0：车贷标；1：净值标；2：秒标；4：渠道标 ; 5流转标")
    private Integer type;

    @ApiModelProperty("锁定状态；0：未锁定；1：已锁定（不能手动投标）")
    private Boolean lockStatus;

    @ApiModelProperty("担保标识")
    private Boolean isVouch;

    @ApiModelProperty("抵押标识")
    private Boolean isMortgage;

    @ApiModelProperty("质押标识")
    private Boolean isImpawn;

    @ApiModelProperty("续贷标识")
    private Boolean isContinued;

    @ApiModelProperty("赎楼标识")
    private Boolean isConversion;

    @ApiModelProperty("新手标识")
    private Boolean isNovice;

    @ApiModelProperty("流转标标识")
    private Boolean isFlow;

    @ApiModelProperty("账号名")
    private String userName;

    @ApiModelProperty("标名")
    private String BorrowName;

    @ApiModelProperty("是否需要密码")
    private boolean isPassWord;

    @ApiModelProperty("发布时间")
    private String releaseAt;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("隱藏最低投標金額")
    private Integer hideLowMoney;

    @ApiModelProperty("债权id; 如果当前是普通标；忽略此字段")
    private Long transferId;

    @ApiModelProperty("tenderId")
    private Long tenderId;

    @ApiModelProperty
    private Long borrowId;

    @ApiModelProperty("当状态还款中 为满标计息时间")
    private String recheckAt;

}