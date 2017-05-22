package com.gofobao.framework.member.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/17.
 */
@Data
@ApiModel
public class VoRegisterReq  extends VoBaseReq {
    @JsonIgnore
    private String channel;
    private String cardId;//身份证号码
    private String username;//用户昵称
    private String mobile;//手机号码
    private String cardNo;//绑定银行卡号
}
