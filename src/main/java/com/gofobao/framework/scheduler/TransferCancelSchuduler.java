package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.common.data.LeSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoEndTransfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/10/9.
 */
@Component
@Slf4j
public class TransferCancelSchuduler {

    @Autowired
    private TransferService transferService;
    @Autowired
    private TransferBiz transferBiz;

    @Scheduled(cron = "0 0 21 * * ? ")
    public void process() {
        //1.查询当日以前未审核、未投满的债权转让通过的债券转让
        Date flagAt = new Date();
        flagAt = DateHelper.beginOfDate(flagAt);
        Specification<Transfer> ts = Specifications
                .<Transfer>and()
                .predicate(new LeSpecification("createdAt", new DataObject(flagAt)))
                .eq("type", 0)
                .in("state", 0, 1)
                .build();
        List<Transfer> transferList = transferService.findList(ts);
        if (CollectionUtils.isEmpty(transferList)) {
            return;
        }
        //筛选出需要取消的债权转让
        List<Transfer> qualifiedTransferList = transferList.stream().filter(transfer -> transfer.getTransferMoneyYes() < transfer.getTransferMoney()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(qualifiedTransferList)) {
            return;
        }
        //2.进行取消操作
        for (Transfer transfer : qualifiedTransferList) {
            VoEndTransfer voEndTransfer = new VoEndTransfer();
            voEndTransfer.setTransferId(transfer.getId());
            voEndTransfer.setUserId(transfer.getUserId());
            try {
                transferBiz.endTransfer(voEndTransfer);
            } catch (Exception e) {
                log.error("TransferCancelSchuduler process：", e);
            }
        }
    }
}
