package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/8.
 */
@ApiModel
@Data
public class VoViewFindUserAddressList extends VoBaseResp {
    @ApiModelProperty("用户收货地址列表")
    public List<VoUserAddress> userAddressList;
}
