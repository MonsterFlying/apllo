package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.Aleve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * eve服务
 */
public interface AleveService {

    List<Aleve> findByTranno(String tranno);

    Aleve save(Aleve aleve);

    /**
     * 查询总数
     * @param specification
     * @return
     */
    long count(Specification<Aleve> specification);

    Page<Aleve> findAll(Specification<Aleve> specification, Pageable pageable);

    List<Aleve> findAll(Specification<Aleve> specification) ;
    Long countOfDateAndTranstype(String date, String type);

    /**
     * 根据类型查询时间和类型
     * @param date
     * @param transtype
     * @param pageable
     * @return
     */
    Page<Aleve> findByDateAndTranstype(String date, String transtype, Pageable pageable);
}
