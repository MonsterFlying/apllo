package com.gofobao.framework.lend.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取有草出借借款列表视图类
 * Created by Max on 2017/3/31.
 */
@ApiModel("获取有草出借借款列表视图类")
@Data
public class VoGetPickLendList extends Page{
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty(name = "lendId", value = "有草出借Id", dataType = "int", required = true)
    @NotNull(message = "有草出借Id不能为空!")
    private Long lendId;

}
