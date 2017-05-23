package com.gofobao.framework.currency.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.currency.vo.request.VoConvertCurrencyReq;
import com.gofobao.framework.currency.vo.request.VoListCurrencyReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/23.
 */
public interface CurrencyBiz {

    ResponseEntity<VoBaseResp> list(VoListCurrencyReq voListCurrencyReq);

    /**
     * 兑换广福币
     * @return
     */
    ResponseEntity<Integer> convert(VoConvertCurrencyReq voConvertCurrencyReq);
}
