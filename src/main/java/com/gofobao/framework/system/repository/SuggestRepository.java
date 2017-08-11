package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.Suggest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by admin on 2017/8/10.
 */
@Repository
public interface SuggestRepository extends JpaRepository<Suggest,Long> ,JpaSpecificationExecutor<Suggest> {
}
