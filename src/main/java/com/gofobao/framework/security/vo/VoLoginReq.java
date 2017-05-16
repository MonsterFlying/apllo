package com.gofobao.framework.security.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by Max on 2017/5/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoLoginReq implements Serializable{
    private String account;
    private String password;
    private String captcha ;
    private Integer source = 0 ;
}
