package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.NewEve;

public interface NewEveService {

    NewEve findTopByOrderno(String orderno);

    /**
     * 保存EVE数据
     * @param newEve
     */
    NewEve save(NewEve newEve);

    /**
     * 查询某时间的某种资金交易记录总条数
     * @param transtype
     * @param date
     * @return
     */
    long countByTranstypeAndQueryTime(String transtype, String date);
}
