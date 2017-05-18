package com.gofobao.framework.member.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/17.
 */
@Data
public class VoRegisterReq {
    @JsonIgnore
    private String channel;

}
