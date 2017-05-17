package com.gofobao.framework.member.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Max on 2017/5/17.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoBasicUserInfo {
    private String username ;
    private String phone;
    private String email ;
    private boolean phoneState;
    private boolean emailState ;
    private boolean bankState ;
    private boolean realname ;
}
