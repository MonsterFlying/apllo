package com.gofobao.framework.borrow.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by admin on 2017/5/17.
 */
@Data
@ApiModel("首页标列表")
public class VoViewBorrowList implements Serializable {
    @ApiModelProperty("标ID")
    private Long id;
    @ApiModelProperty("1.待发布 2.还款中 3.招标中 4.已完成 5.已过期 6.待复审")
    private Integer status;
    @ApiModelProperty("标类型: type:-1：全部 0：车贷标；1：净值标；2：秒标；4：渠道标 ; 5流转标")
    private Integer type;
    @ApiModelProperty("标名")
    private String name;
    @ApiModelProperty("融资金额")
    private String money;
    @ApiModelProperty("还款方式；0：按月分期；1：一次性还本付息；2：先息后本")
    private Integer repayFashion;
    @ApiModelProperty("借款期限；当还款方式=1时期限单位为天否则为月")
    private String timeLimit;
    @ApiModelProperty("年化利率")
    private String apr;
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
    @ApiModelProperty("满标金额")
    private String moneyYes;
    @ApiModelProperty("投标笔数")
    private Integer tenderCount;
    @ApiModelProperty("速度")
    private Double spend;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("剩余时间")
    private Long surplusSecond;
    @ApiModelProperty("头像")
    private String avatar;
    @ApiModelProperty("发布时间")
    private String releaseAt;
    @ApiModelProperty("是否加密")
    private Boolean isPassWord;

}
