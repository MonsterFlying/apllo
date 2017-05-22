package com.gofobao.framework.asset.repostitory;
import com.gofobao.framework.asset.entity.AssetLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by admin on 2017/5/22.
 */
@Repository
public interface AssetLogRepository extends JpaRepository<AssetLog,Long> {


    Page<AssetLog> findByUserIdAndType(Integer userId,String type, Pageable pageable);

    Page<AssetLog> findByUserId(Integer userId,Pageable pageable);
}
