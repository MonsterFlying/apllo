package com.gofobao.framework.starfire.tender.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import lombok.Data;

/**
 * Created by master on 2017/9/28.
 */
@Data
public class UserTenderQuery extends BaseRequest {

    private String platform_uid;

    private String start_time;

    private String end_time;

}
