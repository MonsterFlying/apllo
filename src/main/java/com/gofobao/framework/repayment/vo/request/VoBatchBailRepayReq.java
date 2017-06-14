package com.gofobao.framework.repayment.vo.request;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/13.
 */
@Data
public class VoBatchBailRepayReq {
    private Long repaymentId;
    private Double interestPercent;
}
