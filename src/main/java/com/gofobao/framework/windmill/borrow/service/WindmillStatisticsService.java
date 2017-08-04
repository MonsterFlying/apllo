package com.gofobao.framework.windmill.borrow.service;

import com.gofobao.framework.windmill.borrow.vo.response.ByDayStatistics;

/**
 * Created by admin on 2017/8/3.
 */
public interface WindmillStatisticsService {


     ByDayStatistics bySomeDayStatistics(String date);


}
