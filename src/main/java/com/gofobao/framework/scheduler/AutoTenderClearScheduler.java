package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LtSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/7/10.
 */
public class AutoTenderClearScheduler {

    public static final int EXPIRE_DAY = 100;//一百天后过期

    @Autowired
    private AutoTenderService autoTenderService;

    @Scheduled(cron = "0 4 0 * * ? ")
    public void process() {
        do {

            Specification<AutoTender> ats = Specifications
                    .<AutoTender>and()
                    .predicate(new LtSpecification("autoAt", new DataObject(DateHelper.subDays(new Date(), EXPIRE_DAY))))
                    .build();
            List<AutoTender> autoTenderList = autoTenderService.findList(ats);
            if (CollectionUtils.isEmpty(autoTenderList)) {
                break;
            }

            //删除过期自动投标
            autoTenderService.delete(autoTenderList);

            //更新自动投标序号
            autoTenderService.updateAutoTenderOrder();
        } while (false);
    }
}
