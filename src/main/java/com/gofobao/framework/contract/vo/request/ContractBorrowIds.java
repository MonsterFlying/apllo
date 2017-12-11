package com.gofobao.framework.contract.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ContractBorrowIds {

    private Long userId;

    @ApiModelProperty("1:投标合同; 2:摘转合同")
    private Integer type;
}
