package com.gofobao.framework.award.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/19.
 */
@Data
public class CouponTackeRes extends VoBaseResp {

    @ApiModelProperty("第三方请求地址")
    private String responseUrl="";


}
