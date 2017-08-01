package com.gofobao.framework.windmill.user.vo.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by admin on 2017/7/31.
 */
@Data
public class UserRegisterReq {

    @NotNull
    private String from;

    @NotNull
    private String wrb_user_id;

    @NotNull
    private String pf_user_name;

    private String email;


    @NotNull
    private String mobile;


    @NotNull
    private String id_no;

    @NotNull
    private String true_name;


    private String target_url;

}
