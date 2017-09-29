package com.gofobao.framework.starfire.user.vo.response;

import com.gofobao.framework.starfire.common.response.BaseResponse;
import lombok.Data;

/**
 * Created by master on 2017/9/26.
 */
@Data
public class RegisterQueryRes extends BaseResponse {

    private String mobile;

    private String register_token = "";

    private String platform_uid = "";

    private String isRealNameAuthentic = "";

}
