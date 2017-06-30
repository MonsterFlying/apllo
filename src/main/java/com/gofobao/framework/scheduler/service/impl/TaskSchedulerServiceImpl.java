package com.gofobao.framework.scheduler.service.impl;

import com.gofobao.framework.scheduler.entity.TaskScheduler;
import com.gofobao.framework.scheduler.repository.TaskSchedulerRepository;
import com.gofobao.framework.scheduler.service.TaskSchedulerService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Created by Administrator on 2017/6/30 0030.
 */
@Service
public class TaskSchedulerServiceImpl implements TaskSchedulerService {

    @Autowired
    TaskSchedulerRepository taskSchedulerRepository ;

    @Override
    public List<TaskScheduler> findByType(int type, Pageable pageable) {
        Optional<List<TaskScheduler>> optional = Optional.ofNullable(taskSchedulerRepository.findByTypeAndDel(type, 0, pageable)) ;
        return optional.orElse(Lists.newArrayList()) ;
    }

    @Override
    public TaskScheduler save(TaskScheduler taskScheduler) {
        return taskSchedulerRepository.save(taskScheduler);
    }
}
