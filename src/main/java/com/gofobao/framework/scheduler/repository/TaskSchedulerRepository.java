package com.gofobao.framework.scheduler.repository;

import com.gofobao.framework.scheduler.entity.TaskScheduler;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 状态查询
 * Created by Administrator on 2017/6/30 0030.
 */
@Repository
public interface TaskSchedulerRepository extends JpaRepository<TaskScheduler, Long>{
    List<TaskScheduler> findByTypeAndDel(int type, int del, Pageable pageable);

    List<TaskScheduler> findByTypeAndTaskData(Integer type, String taskData);
}
