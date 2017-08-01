package com.gofobao.framework.windmill.user.vo.request;

import lombok.Data;

/**
 * Created by admin on 2017/8/1.
 */
@Data
public class BindLoginReq {
    private String userName;

    private String password;

    private String param;

}
