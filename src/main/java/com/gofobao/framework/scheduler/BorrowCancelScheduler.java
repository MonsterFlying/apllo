package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LeSpecification;
import com.gofobao.framework.helper.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/7/5.
 */
@Component
@Slf4j
public class BorrowCancelScheduler {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowBiz borrowBiz;

    @Scheduled(fixedRate = 50000)
    public void process() {
        log.info("取消借款任务调度启动");
        Specification<Borrow> bs = Specifications
                .<Borrow>and()
                .eq("status", 1)
                .predicate(new LeSpecification("releaseAt", new DataObject(DateHelper.beginOfDate(DateHelper.subDays(new Date(), 1)))))
                .build();
        int pageIndex = 0;
        int pageSize = 50;
        List<Borrow> borrowList = null;
        Pageable pageable = null;
        do {
            pageable = new PageRequest(pageIndex++, pageSize, new Sort(Sort.Direction.ASC, "id"));
            borrowList = borrowService.findList(bs, pageable);
            //筛选已过期的标的
            borrowList = borrowList.stream().filter(borrow ->
                    //当前时间>发布时间（时间+1）
                    new Date().getTime() > DateHelper.addDays(DateHelper.beginOfDate(borrow.getReleaseAt()), borrow.getValidDay() + 1).getTime()).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(borrowList)) {
                break;
            }
            borrowList.stream().forEach(borrow -> {
                VoCancelBorrow voCancelBorrow = new VoCancelBorrow();
                voCancelBorrow.setUserId(borrow.getUserId());
                voCancelBorrow.setBorrowId(borrow.getId());
                try {
                    borrowBiz.cancelBorrow(voCancelBorrow);
                } catch (Exception e) {
                    log.error("BorrowCancelScheduler process 取消标的失败：", e);
                }
            });

        } while (borrowList.size() >= pageSize);
    }
}
