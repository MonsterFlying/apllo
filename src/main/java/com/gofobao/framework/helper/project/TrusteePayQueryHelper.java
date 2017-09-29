package com.gofobao.framework.helper.project;

import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrusteePayQueryHelper {
    @Autowired
    JixinManager jixinManager;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;


    private static final Gson GSON = new Gson();

    /**
     * 查询委托支付
     *
     * @param accountId 账户ID
     * @param productId 商品ID
     * @param retryNum  重置机制
     * @return
     */
    public TrusteePayQueryResp queryTrusteePayQuery(String accountId, String productId, int retryNum) {
        Preconditions.checkNotNull(accountId, "查询委托支付, 电子账户信息为空");
        Preconditions.checkNotNull(productId, "查询委托支付, 登记productId信息为空");

        TrusteePayQueryReq trusteePayQueryReq = new TrusteePayQueryReq();
        trusteePayQueryReq.setAccountId(accountId);
        trusteePayQueryReq.setProductId(productId);

        if (retryNum <= 0) {
            log.error(String.format("查询即信委托信息BUG, 数据[%s]", GSON.toJson(trusteePayQueryReq)));
            return null;
        }

        TrusteePayQueryResp trusteePayQueryResp = jixinManager.send(JixinTxCodeEnum.TRUSTEE_PAY_QUERY, trusteePayQueryReq, TrusteePayQueryResp.class);
        if (JixinResultContants.isNetWordError(trusteePayQueryResp)) {
            log.error(String.format("查询即信委托信息网络异常, 数据[%s]", GSON.toJson(trusteePayQueryReq)));
            return queryTrusteePayQuery(accountId, productId, retryNum - 1);
        }

        if (JixinResultContants.isBusy(trusteePayQueryResp)) {
            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {

            }
            return queryTrusteePayQuery(accountId, productId, retryNum - 1);
        }

        return trusteePayQueryResp;
    }
}
