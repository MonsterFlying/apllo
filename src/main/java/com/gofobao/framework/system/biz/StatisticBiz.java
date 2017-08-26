package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.vo.response.NewIndexStatisics;
import com.gofobao.framework.system.vo.response.OperateDataStatistics;
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

    /**
     * 查询移动端首页统计数据
     * @return
     */
    NewIndexStatisics queryMobileIndexData();


    /**
     * 运营数据
     * @return
     */
   ResponseEntity<OperateDataStatistics>  queryOperateData();

}
