package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.NewAssetLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface NewAssetLogRepository extends JpaRepository<NewAssetLog, Long>, JpaSpecificationExecutor<NewAssetLog> {
    NewAssetLog findById(long id);


    @Query(value = "SELECT " +
            "    * " +
            "FROM " +
            "    gfb_new_asset_log " +
            "WHERE " +
            "    create_time > ?1 AND create_time < ?2 " +
            "GROUP BY user_id " +
            "ORDER BY ?#{#pageable}",
            nativeQuery = true)
    List<NewAssetLog> findByDate(String beginDate, String endDate, Pageable pageable);

    @Query(value = "SELECT " +
            "COUNT( DISTINCT user_id)  " +
            "FROM " +
            "    gfb_new_asset_log " +
            "WHERE " +
            "    create_time > ?1 AND create_time < ?2 ",
            nativeQuery = true)
    long countByDate(String beginDate, String endDate);
}
