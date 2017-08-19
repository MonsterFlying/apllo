package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by admin on 2017/8/19.
 */
@Repository
public interface AreaRepository extends JpaSpecificationExecutor<Area> ,JpaRepository<Area,Long> {
}
