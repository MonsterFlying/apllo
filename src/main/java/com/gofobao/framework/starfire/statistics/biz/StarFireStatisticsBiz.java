package com.gofobao.framework.starfire.statistics.biz;

import com.gofobao.framework.starfire.statistics.vo.request.StatisticsQuery;
import com.gofobao.framework.starfire.statistics.vo.response.StatisticsDataRes;

/**
 * Created by master on 2017/9/29.
 */
public interface StarFireStatisticsBiz {

    /**
     *
     * @param statisticsQuery
     * @return
     */
      StatisticsDataRes query(StatisticsQuery statisticsQuery);

}
