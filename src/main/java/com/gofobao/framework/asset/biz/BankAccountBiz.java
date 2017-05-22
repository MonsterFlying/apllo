package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.VoUserBankListReq;
import com.gofobao.framework.asset.vo.response.VoUserBankListResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface BankAccountBiz {

    /**
     * 获取用户银行卡列表
     * @param voUserBankListReq
     * @return
     */
    ResponseEntity<VoUserBankListResp> listUserBank(VoUserBankListReq voUserBankListReq);
}
