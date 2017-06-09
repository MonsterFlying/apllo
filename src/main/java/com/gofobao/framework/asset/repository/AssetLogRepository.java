package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.AssetLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/5/22.
 */
@Repository
public interface AssetLogRepository  extends JpaRepository<AssetLog, Long>, JpaSpecificationExecutor<AssetLog>{

    /**
     * user=? and type=？ and createAt>=? and createAt <=?
     * @param userId
     * @param type
     * @param pageable
     * @return
     */
  //  Page<AssetLog> findByUserIdAndTypeAndCreateAtLessThanEqualAndCreateAtGreaterThanEqual(Integer userId,String type,String startTime,String endTime, Pageable pageable);

    /**
     * user=? and createAt>=? and createAt <=?
     * @param userId
     * @param pageable
     * @return
     */
  //  Page<AssetLog> findByUserIdAndCreateAtLessThanEqualAndCreateAtGreaterThanEqual(Integer userId,String startTime,String endTime,Pageable pageable);


    /**
     *
     * @param userId
     * @param type
     * @return
     */
    List<AssetLog> findByUserIdAndTypeIs(Long userId, String type);

}
