package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.helper.MultiCaculateHelper;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.service.StatisticService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by Max on 17/6/2.
 */
@Component
@Slf4j
public class StatisticBizImpl implements StatisticBiz {
    @Autowired
    StatisticService statisticService ;

    private static final Gson GSON = new Gson() ;
    @Override
    @Transactional
    public boolean caculate(Statistic changeEntity) throws Exception {
        Preconditions.checkNotNull(changeEntity, "StatisticBizImpl.caculate: changeEntity is empty") ;
        log.info(String.format("全站统计增加: %s",GSON.toJson(changeEntity)));
        Statistic statistic =  statisticService.findLast() ;
        Preconditions.checkNotNull(statistic, "StatisticBizImpl.caculate: statistic is empty") ;
        MultiCaculateHelper.caculate(Statistic.class, statistic, changeEntity);
        statistic.setUpdatedAt(new Date());
        statisticService.save(statistic) ;
        return true;
    }
}
