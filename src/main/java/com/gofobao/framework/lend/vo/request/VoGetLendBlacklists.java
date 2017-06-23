package com.gofobao.framework.lend.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 获取有草出借黑名单列表
 * Created by Max on 2017/3/31.
 */
@ApiModel("获取有草出借黑名单列表")
@Data
public class VoGetLendBlacklists extends Page {

    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

}
