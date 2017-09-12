package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.ThirdBatchDealLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.awt.print.Pageable;
import java.util.List;

/**
 * Created by Zeke on 2017/9/12.
 */
public interface ThirdBatchDealLogService {

    ThirdBatchDealLog findById(long id);

    List<ThirdBatchDealLog> findList(Specification<ThirdBatchDealLog> specification);

    List<ThirdBatchDealLog> findList(Specification<ThirdBatchDealLog> specification, Sort sort);

    List<ThirdBatchDealLog> findList(Specification<ThirdBatchDealLog> specification, Pageable pageable);

    long count(Specification<ThirdBatchDealLog> specification);

    ThirdBatchDealLog save(ThirdBatchDealLog ThirdBatchDealLog);

    List<ThirdBatchDealLog> save(List<ThirdBatchDealLog> ThirdBatchDealLogList);

}
