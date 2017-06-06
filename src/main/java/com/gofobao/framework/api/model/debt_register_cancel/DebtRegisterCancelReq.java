package com.gofobao.framework.api.model.debt_register_cancel;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class DebtRegisterCancelReq extends JixinBaseRequest{
    /**
     * 由P2P生成，必须保证唯一
     */
    private String accountId;
    /**
     * 募集日 YYYYMMDD
     */
    private String raiseDate;
    /**
     * 请求方保留 选填
     */
    private String acqRes;
}
