package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.entity.IncrStatistic;
import com.gofobao.framework.system.repository.IncrStatisticRepository;
import com.gofobao.framework.system.service.IncrStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Max on 17/6/2.
 */
@Service
public class IncrStatisticServiceImpl implements IncrStatisticService{

    @Autowired
    IncrStatisticRepository  incrStatisticRepository ;

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public IncrStatistic findOneByDate(Date data) {

        Date begin = DateHelper.beginOfDate(data);
        Date end = DateHelper.endOfDate(data);
        Pageable pageable = new PageRequest(0, 1, new Sort(new Sort.Order(Sort.Direction.DESC, "id"))) ;
        Page<IncrStatistic> all = incrStatisticRepository.findAll(pageable);

        IncrStatistic incrStatistic = null ;
        if(all.getTotalElements() == 0){ // 第一次常见
            incrStatistic = new IncrStatistic();
            incrStatistic.setDate(data);
            incrStatistic = incrStatisticRepository.save(incrStatistic) ;
        }else{
            IncrStatistic oldIncrStatistic = all.getContent().get(0);
            Date oldDate = oldIncrStatistic.getDate();
            boolean state = DateHelper.isBetween(oldDate, begin, end);
            if(state){
                return oldIncrStatistic ;
            }else{
                IncrStatistic newIncrStatistic = new IncrStatistic();
                newIncrStatistic.setRegisterTotalCount(oldIncrStatistic.getRegisterTotalCount() == null ? 0 : oldIncrStatistic.getRegisterTotalCount());  // 注册总数
                newIncrStatistic.setRealRegisterTotalCount(oldIncrStatistic.getRealRegisterTotalCount() == null ? 0 : oldIncrStatistic.getRealRegisterTotalCount() );  // 实名注册总数
                newIncrStatistic.setTenderJzTotalCount(oldIncrStatistic.getTenderJzTotalCount() == null ? 0 : oldIncrStatistic.getTenderJzTotalCount());
                newIncrStatistic.setTenderLzTotalCount(oldIncrStatistic.getTenderLzTotalCount() == null ? 0 : oldIncrStatistic.getTenderLzTotalCount());
                newIncrStatistic.setTenderMiaoTotalCount(oldIncrStatistic.getTenderMiaoTotalCount() == null ? 0 : oldIncrStatistic.getTenderMiaoTotalCount());
                newIncrStatistic.setTenderQdTotalCount(oldIncrStatistic.getTenderQdTotalCount() == null ? 0 : oldIncrStatistic.getTenderQdTotalCount());
                newIncrStatistic.setTenderTjTotalCount(oldIncrStatistic.getTenderTjTotalCount() == null ? 0 : oldIncrStatistic.getTenderTjTotalCount());
                newIncrStatistic.setTenderTotal(oldIncrStatistic.getTenderTotal() == null ? 0 : oldIncrStatistic.getTenderTotal());
                newIncrStatistic.setDate(data);
                incrStatistic = incrStatisticRepository.save(newIncrStatistic) ;
            }
        }
        return incrStatistic;
    }

    @Override
    public IncrStatistic save(IncrStatistic dbIncrStatistic) {
        return incrStatisticRepository.save(dbIncrStatistic) ;
    }

    @Autowired
    private EntityManager entityManager;

    /**
     * 注册人数统计
     * @return
     */
    @Override
    public BigDecimal registerTotal() {
        String sql="SELECT SUM(i.register_count) from gfb_incr_statistic as i";
        Query query=entityManager.createNativeQuery(sql);
      return (BigDecimal) query.getSingleResult();
    }
}
