package com.gofobao.framework.finance.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import lombok.Data;

/**
 * Created by Zeke on 2017/8/14.
 */
@Data
public class VoViewFinancePlanTender extends VoBaseResp{
    private FinancePlanBuyer financePlanBuyer;
}


