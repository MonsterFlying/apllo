package com.gofobao.framework.scheduler.repository;

import com.gofobao.framework.scheduler.entity.Count;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by xin on 2017/12/13.
 */
@Repository
public interface CountRepository extends JpaRepository<Count, Long> {
    Count findByCountDate(Date date);
}
