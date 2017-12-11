package com.gofobao.framework.contract.vo.request;

import com.gofobao.framework.contract.contants.ContractContants;
import lombok.Data;

/**
 * Created by master on 2017/11/16.
 */
@Data
public class ContractDetailsReq {

    private Long userId;

    private Integer documentType = ContractContants.DESENSITIZE;

    private String contractId;


}
