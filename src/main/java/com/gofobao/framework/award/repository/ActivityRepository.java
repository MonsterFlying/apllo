package com.gofobao.framework.award.repository;

import com.gofobao.framework.award.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by admin on 2017/6/30.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity,Long>,JpaSpecificationExecutor<Activity> {
}
