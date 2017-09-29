package com.gofobao.framework.starfire.user.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/27.
 */
@Data
public class RegisterModel extends BaseRequest {

    @ApiModelProperty("用户手机号")
    private String mobile;

    @ApiModelProperty("星火智投用户id")
    private String user_id;

    @ApiModelProperty("用户真实姓名")
    private String user_name;

    @ApiModelProperty("用户身份证号码")
    private String user_identity;


}
