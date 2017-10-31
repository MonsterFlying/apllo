package com.gofobao.framework.as.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.as.biz.RedpackStatementBiz;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.collection.vo.response.web.Collection;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class RedpackStatementBizImpl implements RedpackStatementBiz {

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    NewEveService newEveService;

    @Autowired
    NewAleveService newAleveService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    MqHelper mqHelper;

    @Autowired
    private NewAssetLogService newAssetLogService;

    @Override
    public boolean offlineStatement(Long userId, Date date) throws Exception {
        log.info("红包记录匹配 开始");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "userthirdAccount record is null");
        Preconditions.checkNotNull(date, "date is null");
        List<NewEve> thirdRecords = null;

        try {
            // 红包转入
            String type = "7833";
            thirdRecords = newEveService.findAllByTranTypeAndDateAndAccountId(type, userThirdAccount.getAccountId(), date);
        } catch (Exception e) {
            log.warn("[红包对账]: 查询存管红包流水为空", e);
            return false;
        }

        List<NewAssetLog> newAssetLogs = findAssetLog(userId, date);
        if (CollectionUtils.isEmpty(newAssetLogs)) {
            log.warn("[红包对账] 本地金额为零");
        }

        if (CollectionUtils.isEmpty(thirdRecords)
                && CollectionUtils.isEmpty(newAssetLogs)) {
            log.warn("存管流水与本地流水相持平, 无需进行对账");
            return true;
        }



        log.info("红包记录匹配 结束");
        return false;
    }

    /**
     * 查找本地流水文件
     *
     * @param userId
     * @param date
     * @return
     */
    private List<NewAssetLog> findAssetLog(Long userId, Date date) {
        Date beginDate = DateHelper.endOfDate(DateHelper.subDays(date, 1)); //  查询开始时间
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(date, 1)); // 查询结束时间
        Specification<NewAssetLog> newAssetLogSpecification = Specifications
                .<NewAssetLog>and()
                // 用户Id
                .eq("userId", userId)
                // 红包类型
                .eq("type", 7833)
                // 有效
                .eq("del", 0)
                // 时间
                .between("createTime", new Range<>(beginDate, endDate))
                .build();
        List<NewAssetLog> all = newAssetLogService.findAll(newAssetLogSpecification);
        Optional<List<NewAssetLog>> optional = Optional.ofNullable(all);
        return optional.orElse(Lists.newArrayList());
    }
}
