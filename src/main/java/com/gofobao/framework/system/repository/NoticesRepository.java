package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.Notices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Max on 17/6/5.
 */
@Repository
public interface NoticesRepository extends JpaRepository<Notices, Long>{
}
