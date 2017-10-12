package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.JixinAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JixinAssetService {

    /**
     * 根据账户查询即信账户信息
     * @param cardnbr
     * @return
     */
    JixinAsset findTopByAccountId(String cardnbr);

    JixinAsset save(JixinAsset jixinAsset);

    Page<Object[]> findAllForPrint(Pageable pageable);
}
