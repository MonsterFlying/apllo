package com.gofobao.framework.api.model.contract_debttemplate_query;

import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import lombok.Data;

/**
 * @author master
 * @date 2017/11/16
 */
@Data
public class TemplateQueryResponse extends ContractBaseResponse {

    private String template;

    private String tradeType;

    private String acqRes;

}
