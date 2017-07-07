package com.gofobao.framework.scheduler.biz.impl;

import com.gofobao.framework.scheduler.biz.TaskSchedulerBiz;
import com.gofobao.framework.scheduler.entity.TaskScheduler;
import com.gofobao.framework.scheduler.service.TaskSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2017/6/30 0030.
 */
@Component
public class TaskSchedulerBizImpl implements TaskSchedulerBiz {

    @Autowired
    TaskSchedulerService taskSchedulerService ;

    @Override
    public List<TaskScheduler> findByType(int pageIndex, int pageSize, int type) {
        Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "id"))) ;
        return taskSchedulerService.findByType(type, pageable) ;
    }

    @Override
    public TaskScheduler save(TaskScheduler taskScheduler) {
        List<TaskScheduler> taskSchedulers = taskSchedulerService.findByTypeAndTaskData(taskScheduler.getType(), taskScheduler.getTaskData()) ;
        return taskSchedulerService.save(taskScheduler) ;
    }
}
