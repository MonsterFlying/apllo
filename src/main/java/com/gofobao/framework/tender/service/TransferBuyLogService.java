package com.gofobao.framework.tender.service;

import com.gofobao.framework.tender.entity.TransferBuyLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/7/31.
 */
public interface TransferBuyLogService {
    List<TransferBuyLog> findList(Specification<TransferBuyLog> specification);

    List<TransferBuyLog> findList(Specification<TransferBuyLog> specification, Sort sort);

    List<TransferBuyLog> findList(Specification<TransferBuyLog> specification, Pageable pageable);

    long count(Specification<TransferBuyLog> specification);

    TransferBuyLog findById(long id);

    TransferBuyLog save(TransferBuyLog transferBuyLog);

    List<TransferBuyLog> save(List<TransferBuyLog> transferBuyLogList);
}
