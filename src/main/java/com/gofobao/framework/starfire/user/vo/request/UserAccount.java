package com.gofobao.framework.starfire.user.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import lombok.Data;

/**
 * Created by master on 2017/9/29.
 */
@Data
public class UserAccount extends BaseRequest {

    private String platform_uid;

    private String start_time;

    private String end_time;

}
