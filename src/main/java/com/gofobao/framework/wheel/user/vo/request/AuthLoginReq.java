package com.gofobao.framework.wheel.user.vo.request;

import com.gofobao.framework.wheel.common.BaseRequest;
import lombok.Data;

/**
 *
 * @author master
 * @date 2017/10/27
 */

@Data
public class AuthLoginReq extends BaseRequest {

    private String ticket;

    private String target_url;

}
