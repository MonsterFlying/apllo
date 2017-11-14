package com.gofobao.framework.comment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xin on 2017/11/9.
 */
@ApiModel
@Data
public class VoAvatarResp extends VoBaseResp {
    @ApiModelProperty("头像地址全路径")
    private String img ;
}
