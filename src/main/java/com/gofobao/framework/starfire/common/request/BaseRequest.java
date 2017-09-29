package com.gofobao.framework.starfire.common.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by master on 2017/9/26.
 */
@Data
@Component
public  class BaseRequest {

    @ApiModelProperty("渠道编码")
    private String c_code;

    @ApiModelProperty("时间戳")
    private String t_code;

    @ApiModelProperty("流水号")
    private String serial_num;

    @ApiModelProperty("安全数字签名")
    private String sign;


}
