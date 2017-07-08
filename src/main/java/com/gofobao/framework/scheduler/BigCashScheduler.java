package com.gofobao.framework.scheduler;

import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import com.gofobao.framework.scheduler.biz.TaskSchedulerBiz;
import com.gofobao.framework.scheduler.constants.TaskSchedulerConstants;
import com.gofobao.framework.scheduler.entity.TaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 大额提现调度
 * Created by Administrator on 2017/6/30 0030.
 */
@Component
@Slf4j
public class BigCashScheduler {

    @Autowired
    TaskSchedulerBiz taskSchedulerBiz;

    @Autowired
    CashDetailLogBiz cashDetailLogBiz;

    @Scheduled(fixedRate = 20 * 60 * 1000)
    public void process() {
        log.info("提现状态确认调度启动");
        // 查询带调度队列
        int pageSize = 40;
        int pageIndex = 0;
        int type = TaskSchedulerConstants.CASH_FORM;
        int size = 0;
        do {
            long startDate = System.currentTimeMillis();
            List<TaskScheduler> taskSchedulerList = taskSchedulerBiz.findByType(pageIndex, pageSize, type);
            size = taskSchedulerList.size() ;
            taskSchedulerList.forEach(p -> {
                try {
                    boolean b = cashDetailLogBiz.doBigCashForm(p.getTaskData());
                    p.setDoTaskNum(p.getDoTaskNum() + 1);
                    p.setState(b ? 1 : 0);
                    p.setDel(b ? 1 : 0);
                    p.setUpdateAt(new Date());
                    if (p.getTaskNum() <= p.getDoTaskNum()) {
                        p.setDel(1);
                    }

                    taskSchedulerBiz.save(p);
                    log.info(String.format("提现确认查询调度使用时间 %s 毫秒", System.currentTimeMillis() - startDate));
                } catch (Throwable e) {
                    log.error("查询自己调度失败", e);
                }
            });
        } while (size == pageSize);
    }

}