package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LtSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/7/10.
 */
@Component
@Slf4j
public class AutoTenderClearScheduler {

    public static final int EXPIRE_DAY = 100;//一百天后过期

    @Autowired
    private AutoTenderService autoTenderService;

    @Autowired
    private ExceptionEmailHelper exceptionEmailHelper;

    @Scheduled(cron = "0 4 0 * * ? ")
    @Transactional(rollbackOn = Exception.class)
    public void process() {
        try {
            log.info("自动清除过期自动投标规则启动");
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
        }catch (Exception e){
            exceptionEmailHelper.sendException("调度-自动清除过期自动失败",e);
        }
    }
}
