package com.gofobao.framework.windmill.user.vo.respones;

import lombok.Data;

/**
 * Created by admin on 2017/7/31.
 */
@Data
public class UserRegisterRes {

    private Integer retcode;

    private String retmsg;

    private Long pf_user_id;

    private String pf_user_name;

}
