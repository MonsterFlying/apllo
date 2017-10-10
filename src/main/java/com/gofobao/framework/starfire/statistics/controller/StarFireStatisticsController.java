package com.gofobao.framework.starfire.statistics.controller;

import com.gofobao.framework.starfire.statistics.biz.StarFireStatisticsBiz;
import com.gofobao.framework.starfire.statistics.vo.request.StatisticsQuery;
import com.gofobao.framework.starfire.statistics.vo.response.StatisticsDataRes;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by master on 2017/10/10.
 */
@RestController
@RequestMapping("pub/starfire/statistics")
public class StarFireStatisticsController {


    @Autowired
    private StarFireStatisticsBiz starFireStatisticsBiz;

    @RequestMapping("query")
    @ApiOperation("平台汇总数据查询")
    public StatisticsDataRes queryUserTender(StatisticsQuery statisticsQuery) {
        return starFireStatisticsBiz.query(statisticsQuery);
    }
}
