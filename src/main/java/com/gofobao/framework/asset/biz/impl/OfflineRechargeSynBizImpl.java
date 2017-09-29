package com.gofobao.framework.asset.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.biz.OfflineRechargeSynBiz;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewEveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OfflineRechargeSynBizImpl implements OfflineRechargeSynBiz {

    @Autowired
    RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    NewEveService newEveService;


    @Override
    public boolean process(String date) {
        log.info("================================");
        log.info(String.format("线下充值确认, 时间: %s", date));
        log.info("================================");
        String transtype = "7820";  // 线下转账类型
        Specification<NewEve> eveSpecification = Specifications.<NewEve>and()
                .eq("transtype", transtype)
                .build();

        // 查询所有资金变动记录
        return false;
    }
}
