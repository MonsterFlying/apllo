package com.gofobao.framework.collection.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by admin on 2017/5/31.
 */
@Data
public class VoCollectionOrderReq  extends Page{

    private Long userId;

    @ApiModelProperty("时间")
    private String time;
}
