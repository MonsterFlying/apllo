package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.Statistic;

/**
 * Created by Max on 17/6/2.
 */
public interface StatisticService {

    /**
     * 查找最后一条记录， 如果没有就创建
     * @return
     */
    Statistic findLast();

    /**
     * 保存
     * @param statistic
     */
    void save(Statistic statistic);
}
