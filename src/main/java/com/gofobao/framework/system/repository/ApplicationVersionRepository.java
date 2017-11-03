package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.ApplicationVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by master on 2017/10/23.
 */
@Repository
public interface ApplicationVersionRepository extends JpaRepository<ApplicationVersion, Integer> {
    ApplicationVersion findTopByApplicationIdOrderByIdDesc(Integer applicationId);

}
