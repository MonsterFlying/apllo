package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.ThirdBatchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/6/15.
 */
public interface ThirdBatchLogService {

    ThirdBatchLog findById(long id);

    List<ThirdBatchLog> findList(Specification<ThirdBatchLog> specification);

    List<ThirdBatchLog> findList(Specification<ThirdBatchLog> specification, Sort sort);

    List<ThirdBatchLog> findList(Specification<ThirdBatchLog> specification, Pageable pageable);

    long count(Specification<ThirdBatchLog> specification);

    ThirdBatchLog save(ThirdBatchLog thirdBatchLog);

    List<ThirdBatchLog> save(List<ThirdBatchLog> thirdBatchLogList);

    ThirdBatchLog findByBatchNoAndSourceId(String batchNo, Long sourceId);
}
