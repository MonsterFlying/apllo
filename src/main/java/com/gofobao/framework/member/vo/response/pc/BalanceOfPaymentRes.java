package com.gofobao.framework.member.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/11.
 */
@Data
public class BalanceOfPaymentRes extends VoBaseResp {

    @ApiModelProperty("待还")
    private String waitPayment="0";

    @ApiModelProperty("已还")
    private String payment="0";

    @ApiModelProperty("待收")
    private String waitCollection="0";

    @ApiModelProperty("已收")
    private String collection="0";

}
