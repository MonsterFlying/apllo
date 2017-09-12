package com.gofobao.framework.marketing.service;

import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

public interface MarketingRedpackRecordService {

    MarketingRedpackRecord save(MarketingRedpackRecord marketingRedpackRecord);

    List<MarketingRedpackRecord> findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(Long marketingId, Long redpackRuleId, Long userId, Long sourceId);

    /**
     * 根据取消时间,查询红包记录
     * @param startDate
     * @param nowDate
     * @param pageable
     * @return
     */
    List<MarketingRedpackRecord> findByCancelTimeBetween(Date startDate, Date nowDate, Pageable pageable);

    /**
     *  批量保存
     * @param marketingRedpackRecordList
     */
    void save(List<MarketingRedpackRecord> marketingRedpackRecordList);

    /**
     * 查找用户红包记录
     * @param userId
     * @param status
     * @param pageable
     * @return
     */
    List<MarketingRedpackRecord> findByUserIdAndState(Long userId, Integer status, Pageable pageable);

    /**
     *  根据用户id和红包ID
     * @param redPackageId
     * @param userId
     * @param del
     * @return
     */
    MarketingRedpackRecord findTopByIdAndUserIdAndDel(Long redPackageId, Long userId, int del);

    MarketingRedpackRecord findById(long id);

    long count(Specification<MarketingRedpackRecord> specifications);
}
