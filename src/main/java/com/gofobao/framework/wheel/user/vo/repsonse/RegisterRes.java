package com.gofobao.framework.wheel.user.vo.repsonse;

import com.gofobao.framework.wheel.common.BaseResponse;
import lombok.Data;

/**
 *
 * @author master
 * @date 2017/10/27
 */
@Data
public class RegisterRes extends BaseResponse {

    private String pf_user_id;

    private String pf_user_name;

}
