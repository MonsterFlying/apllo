package com.gofobao.framework.starfire.user.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gofobao.framework.starfire.common.response.BaseResponse;
import lombok.Data;

/**
 * Created by master on 2017/9/27.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchLoginTokenRes extends BaseResponse {

    private String login_token;

}
