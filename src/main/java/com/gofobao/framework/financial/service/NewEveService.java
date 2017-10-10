package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.LocalRecord;
import com.gofobao.framework.financial.entity.NewEve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface NewEveService {

    NewEve findTopByOrderno(String orderno);

    /**
     * 保存EVE数据
     * @param newEve
     */
    NewEve save(NewEve newEve);

    /**
     * 查询某时间的某种资金交易记录总条数
     * @param transtype
     * @param date
     * @return
     */
    long countByTranstypeAndQueryTime(String transtype, String date);


    /**
     * 查询EVE数据
     * @param orderno
     * @param date
     * @return
     */
    NewEve findTopByOrdernoAndQueryTime(String orderno, String date);

    /**
     * 查询EVE数据
     * @param cendt
     * @param tranno
     * @return
     */
    NewEve findTopByCendtAndTranno(String cendt, String tranno);

    /**
     * 查询类型和时间
     * @param transtype
     * @param date
     * @param pageable
     * @return
     */
    List<NewEve> findByTranstypeAndQueryTime(String transtype, String date, Pageable pageable);


    /**
     * 根据条件查询类型
     * @param specification
     * @return
     */
    List<NewEve> findAll(Specification<NewEve> specification);


    Page<LocalRecord> findLocalAssetChangeRecord(String beginDate, String endDate, Pageable pageable);
}
