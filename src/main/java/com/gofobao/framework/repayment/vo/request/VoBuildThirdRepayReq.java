package com.gofobao.framework.repayment.vo.request;

import lombok.Data;

/**
 * Created by Zeke on 2017/7/17.
 */
@Data
public class VoBuildThirdRepayReq {
    /* 利息百分比 */
    private Double interestPercent;
    /* 还款用户id */
    private Long userId;
    /* 还款id */
    private Long repaymentId;
    /* 是否用户操作 */
    private Boolean isUserOpen;
    /* 逾期天数 */
    private Integer lateDays;
}
