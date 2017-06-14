package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.vo.response.VoViewIndexStatisticsWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Max on 17/6/2.
 */
public interface StatisticBiz {

    /**
     * 变动统计
     * @param changeEntity
     * @return
     * @throws Exception
     */
    boolean caculate(Statistic changeEntity) throws Exception;


    ResponseEntity<VoViewIndexStatisticsWarpRes> query();

}
