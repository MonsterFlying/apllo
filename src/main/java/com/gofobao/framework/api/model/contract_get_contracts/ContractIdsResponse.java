package com.gofobao.framework.api.model.contract_get_contracts;

import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by master on 2017/11/15.
 */
@Data
public class ContractIdsResponse extends ContractBaseResponse {

    private String txCount;

    private List<SubPacks> subPacks;

    private String acqRes;

}
