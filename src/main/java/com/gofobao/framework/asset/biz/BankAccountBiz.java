package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.response.VoBankTypeInfoResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface BankAccountBiz {

    /**
     * 查找银行卡类型基本信息和限额
     *
     * @param userId
     * @param account
     * @return
     */
    ResponseEntity<VoBankTypeInfoResp> findTypeInfo(Long userId, String account);
}
