package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/20 0020.
 */
@Data
@ApiModel
public class VoServiceResp extends VoBaseResp {
    @ApiModelProperty("隐藏客服电话")
    private String  servicePhoneHide;

    @ApiModelProperty("服务热线")
    private String  workday;

    @ApiModelProperty("客服QQ")
    private String  serviceQQ;

    @ApiModelProperty("客服邮箱")
    private String  serviceEmail;

    @ApiModelProperty("微信公众号")
    private String  wechatCode;

    @ApiModelProperty("官方微博")
    private String  weiboCode;

    @ApiModelProperty("官方QQ群")
    private String  qqGroup;

    @ApiModelProperty("显示客服电话")
    private String  servicePhoneView;

    @ApiModelProperty("总经理微信")
    private String managementWeChat;

}
