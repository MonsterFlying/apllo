package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by master on 2017/10/23.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {


}
