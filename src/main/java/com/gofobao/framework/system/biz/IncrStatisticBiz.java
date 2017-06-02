package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.entity.IncrStatistic;

/**
 * Created by Max on 17/6/2.
 */
public interface IncrStatisticBiz {
    /**
     * 计算统计
     * @param changeEntity
     * @return
     */
    void caculate(IncrStatistic changeEntity) throws Exception ;
}
