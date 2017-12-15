package com.gofobao.framework.as.repository;

import com.gofobao.framework.as.entity.RealtimeAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RealtimeAssetRepository extends JpaRepository<RealtimeAsset, Long> {
    @Query(value = "select sum(NULLIF (jixin_total_amount,0)) from gfb_realtime_asset where user_id in ?1 and create_time <= ?2 GROUP by user_id", nativeQuery = true)
    List<Integer> findUserIdAndUpdated(List<Long> ids, Date endDate);

    @Query(value = "select jixin_total_amount  from gfb_realtime_asset WHERE user_id  NOT IN ?1 AND create_time <= ?2", nativeQuery = true)
    List<Integer> findOtherUserIdAndUpdated(List<Long> ids, Date endDate);
}
