package com.gofobao.framework.currency.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.currency.vo.request.VoConvertCurrencyReq;
import com.gofobao.framework.currency.vo.request.VoListCurrencyReq;
import com.gofobao.framework.currency.vo.response.VoListCurrencyResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/23.
 */
public interface CurrencyBiz {

    ResponseEntity<VoListCurrencyResp> list(VoListCurrencyReq voListCurrencyReq);

    /**
     * 兑换广福币
     *
     * @return
     */
    ResponseEntity<VoBaseResp> convert(VoConvertCurrencyReq voConvertCurrencyReq) throws Exception;
}
