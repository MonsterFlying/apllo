package com.gofobao.framework.scheduler.service;

import com.gofobao.framework.scheduler.entity.TaskScheduler;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by Administrator on 2017/6/30 0030.
 */
public interface TaskSchedulerService {

    List<TaskScheduler> findByType(int type, Pageable pageable);

    TaskScheduler save(TaskScheduler taskScheduler);
}
