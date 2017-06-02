package com.gofobao.framework.collection.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@Data
@ApiModel("回款详情")
public class VoOrderDetailReq {

    @ApiModelProperty("回款id")
    private Long collectionId;
    @ApiModelProperty("用户id")
    private Long userId;


}
