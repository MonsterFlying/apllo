package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
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

    @Scheduled(cron = "0 2 0 * * ? ")
    public void process() {
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
            borrowList = borrowList.stream().filter(borrow ->
                    DateHelper.diffInDays(DateHelper.beginOfDate(new Date()), DateHelper.endOfDate(borrow.getReleaseAt()), false) > borrow.getValidDay()).collect(Collectors.toList());
            try {
                if (CollectionUtils.isEmpty(borrowList)) {
                    break;
                }
                borrowBiz.schedulerCancelBorrow(borrowList);
            } catch (Exception e) {
                log.info("borrowCancelSchedule 取消借款异常：", e);
            }
        } while (borrowList.size() >= pageSize);
    }
}
