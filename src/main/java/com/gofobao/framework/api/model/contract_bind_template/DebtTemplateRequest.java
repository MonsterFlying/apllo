package com.gofobao.framework.api.model.contract_bind_template;

import com.gofobao.framework.api.request.ContractBaseRequest;
import lombok.Data;

/**
 * Created by master on 2017/11/14.
 */
@Data
public class DebtTemplateRequest extends ContractBaseRequest {

    private String productId;

    private String templateId;

    private String tradeType;

    private String acqRes;
}
