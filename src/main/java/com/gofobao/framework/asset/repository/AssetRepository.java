package com.gofobao.framework.asset.repository;


import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.entity.NewAssetLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Asset findByUserId(Long id);

    List<Asset> findByUserIdIn(List<Long> userIds);

    @Query(value = "select sum(use_money) from gfb_asset where user_id not in ?1 and updated_at <= ?2 ", nativeQuery = true)
    Integer findOtherUserMoney(List<Long> ids, Date endDate);

    @Query(value = "select sum(no_use_money) from gfb_asset where user_id not in ?1 and updated_at <= ?2 ", nativeQuery = true)
    Integer findOtherNoUseMoney(List<Long> ids, Date endDate);

    @Query(value = "select sum(finance_plan_money) from gfb_asset where user_id not in ?1 and updated_at <= ?2 ", nativeQuery = true)
    Integer findOtherFinancePlan(List<Long> ids, Date endDate);

    @Query(value = "select * from gfb_asset where user_id = ?1 and updated_at <= ?2", nativeQuery = true)
    Asset findUserIdAndUpDated(long zfh, Date endDate);

    @Query(value = "SELECT asset FROM Asset  asset WHERE asset.updatedAt IS NOT NULL")
    List<Asset> orderByUpdatedAt(Pageable pageRequest);


    // Asset findFirstByOrderByUpdatedAtAscAndIsNotNull();
}
