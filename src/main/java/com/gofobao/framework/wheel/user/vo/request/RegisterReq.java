package com.gofobao.framework.wheel.user.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import lombok.Data;

/**
 *
 * @author master
 * @date 2017/10/27
 */
@Data
public class RegisterReq extends BaseRequest {

    private String cl_user_id;

    private String mobile;

}
