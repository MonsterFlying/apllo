package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/9/12.
 */
@ApiModel
@Data
public class VoViewFindRepayStatusListRes extends VoBaseResp {
    @ApiModelProperty("批次放款集合")
    private List<VoFindRepayStatus> voFindRepayStatusList;
}
