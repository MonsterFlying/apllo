package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.helper.MultiCaculateHelper;
import com.gofobao.framework.system.biz.IncrStatisticBiz;
import com.gofobao.framework.system.entity.IncrStatistic;
import com.gofobao.framework.system.service.IncrStatisticService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by Max on 17/6/2.
 */
@Component
@Slf4j
public class IncrStatisticBizImpl implements IncrStatisticBiz {

    @Autowired
    IncrStatisticService incrStatisticService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void caculate(IncrStatistic changeEntity) throws Exception {
        log.info("IncrStatisticBizImpl.caculate is active");
        Preconditions.checkNotNull(changeEntity, "IncrStatisticBizImpl.caculate:changeEntity is empty ");
        Date now = new Date();
        IncrStatistic dbIncrStatistic = incrStatisticService.findOneByDate(now);
        MultiCaculateHelper.caculate(IncrStatistic.class, dbIncrStatistic, changeEntity);
        incrStatisticService.save(dbIncrStatistic);
        log.info("IncrStatisticBizImpl.caculate success");
    }
}
