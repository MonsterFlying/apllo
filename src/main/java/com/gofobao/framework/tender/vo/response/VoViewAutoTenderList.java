package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Max on 2017/3/21.
 */
@ApiModel
@Data
public class VoViewAutoTenderList {
    @ApiModelProperty("自动投标列表")
    private List<VoAutoTender> autoTenderList;

}
