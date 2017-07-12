package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.vo.response.pc.AssetStatistic;
import com.gofobao.framework.member.vo.response.pc.IncomeEarnedDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface UserCacheService {

    /**
     * 根据id查询UserCache
     * @param id
     * @return
     */
    UserCache findById(Long id);

    UserCache findByUserIdLock(Long userId);

    UserCache save(UserCache userCache);

    UserCache updateById(UserCache userCache);

    List<UserCache> findList(Specification<UserCache> specification);

    List<UserCache> findList(Specification<UserCache> specification, Sort sort);

    List<UserCache> findList(Specification<UserCache> specification, Pageable pageable);

    long count(Specification<UserCache> specification);


    /**
     * pc 用户资产统计
     * @param userId
     * @return
     */
    AssetStatistic assetStatistic(Long userId);


    /**
     * 已转收益统计
     * @param userId
     * @return
     */
    ResponseEntity<IncomeEarnedDetail> incomeEarned(Long userId);
}
