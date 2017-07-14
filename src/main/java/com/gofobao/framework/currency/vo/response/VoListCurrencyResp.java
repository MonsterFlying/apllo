package com.gofobao.framework.currency.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/5/23.
 */
@ApiModel
@Data
public class VoListCurrencyResp extends VoBaseResp {


    /**
     * 总广福币
     */
    @ApiModelProperty("总共广福币")
    private Integer totalCurrency;

    /**
     * 有效广福币
     */
    @ApiModelProperty("有效广福币")
    private Integer availableCurrency;

    /**
     * 已用广福币
     */
    @ApiModelProperty("已用广福币")
    private Integer invalidCurrency;


    @ApiModelProperty("总记录数")
    private Integer totalCount = 0;

    /**
     * 广福币列表
     */
    @ApiModelProperty("广福币列表")
    private List<VoCurrency> voCurrencyList= Lists.newArrayList();

}

