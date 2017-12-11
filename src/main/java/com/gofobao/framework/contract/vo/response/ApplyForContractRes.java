package com.gofobao.framework.contract.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ApplyForContractRes extends VoBaseResp {


    @ApiModelProperty("合同id")
    private String contractId;

    @ApiModelProperty("合同地址")
    private String contractUrl;

}
