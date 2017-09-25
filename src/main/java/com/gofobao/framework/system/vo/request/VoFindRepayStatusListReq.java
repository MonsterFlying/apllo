package com.gofobao.framework.system.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/9/12.
 */
@ApiModel
@Data
public class VoFindRepayStatusListReq extends Page {
    @ApiModelProperty("回款id")
    @NotNull(message = "回款id不能为空!")
    private Long collectionId;
}
