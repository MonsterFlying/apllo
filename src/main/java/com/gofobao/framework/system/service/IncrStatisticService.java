package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.IncrStatistic;

import java.util.Date;

/**
 * Created by Max on 17/6/2.
 */
public interface IncrStatisticService {

    /**
     * 根据时间查询
     * @param data
     * @return
     */
    IncrStatistic findOneByDate(Date data) ;

    IncrStatistic save(IncrStatistic dbIncrStatistic);
}
