package com.gofobao.framework.starfire.user.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/26.
 */
@Data
public class RegisterQuery extends BaseRequest {

    @ApiModelProperty("用户手机号")
    private String mobile;
}
