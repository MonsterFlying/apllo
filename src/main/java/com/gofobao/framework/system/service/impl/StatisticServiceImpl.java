package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.repository.StatisticRepository;
import com.gofobao.framework.system.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import javax.persistence.LockModeType;

/**
 * Created by Max on 17/6/2.
 */
@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    StatisticRepository statisticRepository ;

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Statistic findLast() {
        Pageable pageable = new PageRequest(0, 1, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
        Page<Statistic> all = statisticRepository.findAll(pageable);
        Statistic statistic = null ;
        if(all.getTotalElements() == 0){
            statistic = statisticRepository.save( new Statistic() ) ;
        }else{
            statistic = all.getContent().get(0) ;
        }

        return  statistic ;
    }

    @Override
    public void save(Statistic statistic) {
        statisticRepository.save(statistic) ;
    }
}
