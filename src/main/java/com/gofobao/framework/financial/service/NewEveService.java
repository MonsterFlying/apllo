package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.NewEve;

public interface NewEveService {

    NewEve findTopByOrderno(String orderno);

    /**
     * 保存EVE数据
     * @param newEve
     */
    NewEve save(NewEve newEve);
}
