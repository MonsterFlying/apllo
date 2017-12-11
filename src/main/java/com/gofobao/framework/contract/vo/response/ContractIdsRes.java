package com.gofobao.framework.contract.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @author master
 * @date 2017/11/15
 */
@Data
public class ContractIdsRes extends VoBaseResp {

    private List<ContractId> contracts = Lists.newArrayList();



}
