package com.gofobao.framework.scheduler;

import com.gofobao.framework.asset.biz.CashDetailLogBiz;
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
public class BigCashQueryScheduler {

    @Autowired
    TaskSchedulerBiz taskSchedulerBiz;

    @Autowired
    CashDetailLogBiz cashDetailLogBiz ;

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void process() {
        log.info("大额提现资金确认扣减调动启动");
        // 查询带调度队列
        int pageSize = 40;
        int pageIndex = 0;
        int type = TaskSchedulerConstants.CASH_CANCEL;
        int size;
        do {
            List<TaskScheduler> taskSchedulers = taskSchedulerBiz.findByType(pageIndex, pageSize, type);
            size = taskSchedulers.size();
            // 处理调度
            taskSchedulers.forEach(p -> {
                long startDate = System.currentTimeMillis();
                String taskData = p.getTaskData();
                Preconditions.checkNotNull(taskData, "大额提现资金确认扣减: 数据为空");
                Map<String, String> rs = new Gson().fromJson(taskData, TypeTokenContants.MAP_ALL_STRING_TOKEN);
                Long cashId = Long.parseLong(rs.get("cashId"));

                boolean b = cashDetailLogBiz.doFormCashMoney(cashId, p.getDoTaskNum(), p.getTaskNum()) ;
                p.setDoTaskNum(p.getDoTaskNum() + 1);
                p.setState(b ? 1 : 0);
                p.setDel(b ? 1 : 0);
                p.setUpdateAt(new Date());
                if (p.getTaskNum() <= p.getDoTaskNum()) {
                    p.setDel(1);
                }

                taskSchedulerBiz.save(p);
                log.info(String.format("大额提现资金确认扣减调动 使用时间 %s 毫秒", System.currentTimeMillis() - startDate));
            });
        } while (size == pageSize);
    }

}
