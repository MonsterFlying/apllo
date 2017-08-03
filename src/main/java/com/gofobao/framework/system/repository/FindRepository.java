package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.Find;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FindRepository extends JpaRepository<Find, Long>, JpaSpecificationExecutor<Find> {
}
