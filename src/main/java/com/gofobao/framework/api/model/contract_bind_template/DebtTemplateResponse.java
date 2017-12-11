package com.gofobao.framework.api.model.contract_bind_template;

import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import lombok.Data;

/**
 * Created by master on 2017/11/14.
 */
@Data
public class DebtTemplateResponse extends ContractBaseResponse {

    private String templateId;

    private String productId;

    private String tradeType;

    private String acqRes;

}
