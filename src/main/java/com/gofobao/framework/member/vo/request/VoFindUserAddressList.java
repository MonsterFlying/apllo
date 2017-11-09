package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/11/8.
 */
@Data
@ApiModel
public class VoFindUserAddressList extends Page {
    @ApiModelProperty(hidden = true)
    private Long userId;
}
