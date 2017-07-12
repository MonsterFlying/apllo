package com.gofobao.framework.member.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/11.
 */
@Data
public class NetProceedsDetails {


    @ApiModelProperty("待还利息")
    private  String waitInterest;

    @ApiModelProperty("待还本金")
    private  String waitPrincipal;

}
