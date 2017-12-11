package com.gofobao.framework.api.model.contract_debttemplate_query;

import com.gofobao.framework.api.request.ContractBaseRequest;
import lombok.Data;

/**
 * @author master
 * @date 2017/11/16
 */
@Data
public class TemplateQueryRequest extends ContractBaseRequest {

    private String productId;

    private Integer tradeType;

    private String acqRes;

}
