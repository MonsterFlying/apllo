package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 获取自动投标列表视图类
 * Created by Max on 2017/3/14.
 */
@ApiModel("获取自动投标列表视图类")
@Data
public class VoGetAutoTenderList extends Page {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;


}
