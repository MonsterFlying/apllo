package com.gofobao.framework.asset.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Zeke on 2017/5/22.
 */
@ApiModel
@Data
public class VoJudgmentAvailableReq extends VoBaseReq{
    @NotEmpty
    @ApiModelProperty("检测类型： 1.手机号， 2.邮件, 3,用户名")
    private String checkType ;

    @NotEmpty
    @ApiModelProperty("待检测的值")
    private String checkValue ;
}
