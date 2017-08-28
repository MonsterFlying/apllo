package com.gofobao.framework.api.model.balance_un_freeze;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/6.
 */
@Data
public class BalanceUnfreezeResp extends JixinBaseResponse{
    private String accountId;
    private String orderId;
    private String txAmount;
    private String name;
    private String acqRes;
}
