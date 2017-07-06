package com.gofobao.framework.api.model.balance_freeze;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/6.
 */
@Data
public class BalanceFreezeResp extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 冻结金额
     */
    private String txAmount;
    /**
     * 姓名
     */
    private String name;
    /**
     *
     */
    private String acqRes;
}
