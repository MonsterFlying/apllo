package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.repository.StatisticRepository;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.system.service.IncrStatisticService;
import com.gofobao.framework.system.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by Max on 17/6/2.
 */
@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    StatisticRepository statisticRepository;

    @Autowired
    IncrStatisticService incrStatisticService;

    @Autowired
    DictItemService dictItemService;

    DictValueService dictValueService;

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Statistic findLast() {
        List<Statistic> all = statisticRepository.findAll();
        Statistic statistic = null;
        if (CollectionUtils.isEmpty(all)) {
            statistic = statisticRepository.save(new Statistic());
        } else {
            statistic = all.get(0);
        }
        return statistic;
    }

    @Override
    public void save(Statistic statistic) {
        statisticRepository.save(statistic);
    }

}
