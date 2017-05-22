package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.DictValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface DictValueRepository extends JpaRepository<DictValue,Long>{
}
