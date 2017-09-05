package com.gofobao.framework.scheduler;

import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.scheduler.biz.TaskSchedulerBiz;
import com.gofobao.framework.scheduler.constants.TaskSchedulerConstants;
import com.gofobao.framework.scheduler.entity.TaskScheduler;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 委托状态查询
 * Created by Administrator on 2017/6/30 0030.
 */
@Component
@Slf4j
public class TrusteePayQueryScheduler {

    @Autowired
    TaskSchedulerBiz taskSchedulerBiz;

    @Autowired
    BorrowBiz borrowBiz;

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void process() {
        log.info("委托支付标调动启动");
        // 查询带调度队列
        int pageSize = 40;
        int pageIndex = 0;
        int type = TaskSchedulerConstants.TRUSTEE_PAY_QUERY;
        int size;
        do {
            List<TaskScheduler> taskSchedulers = taskSchedulerBiz.findByType(pageIndex, pageSize, type);
            size = taskSchedulers.size();

            // 处理调度
            taskSchedulers.forEach(p -> {
                long startDate = System.currentTimeMillis();
                String taskData = p.getTaskData();
                Preconditions.checkNotNull(taskData, "委托支付标的信息查询任务数据失败");
                Map<String, String> rs = new Gson().fromJson(taskData, TypeTokenContants.MAP_ALL_STRING_TOKEN);
                Long borrowId = Long.parseLong(rs.get("borrowId"));
                boolean b = borrowBiz.doTrusteePay(borrowId);
                p.setDoTaskNum(p.getDoTaskNum() + 1);
                p.setState(b ? 1 : 0);
                p.setDel(b ? 1 : 0);
                p.setUpdateAt(new Date());
                if (p.getTaskNum() <= p.getDoTaskNum()) {
                    p.setDel(1);
                }

                taskSchedulerBiz.save(p);
                log.info(String.format("委托支付标信息查询调度使用时间 %s 毫秒", System.currentTimeMillis() - startDate));
            });
        } while (size == pageSize);
    }

}
