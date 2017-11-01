package com.gofobao.framework.as.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.as.biz.RedpackStatementBiz;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.financial.entity.NewAleve;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.MoneyHelper;
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

import javax.validation.constraints.NotNull;
import java.util.*;

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
        List<NewAleve> thirdRecords = null;
        if (assetChangeProvider.getRedpackAccountId().equals(userThirdAccount.getUserId())) {
            log.warn("[红包对账] 不支持红包账户对账 ");
            return false;
        }

        try {
            // 红包转入
            String type = "7833";
            thirdRecords = newAleveService.findAllByTranTypeAndDateAndAccountId(type, userThirdAccount.getAccountId(), date);
        } catch (Exception e) {
            log.warn("[红包对账] 查询存管红包流水为空", e);
            return false;
        }

        // 正常流水
        List<NewAleve> okThirdRecords = new ArrayList<>();
        // 拨正流水
        List<NewAleve> cancleThirdRecords = new ArrayList<>();
        for (NewAleve item : thirdRecords) {
            // 拨正数据
            if ("1".equalsIgnoreCase(item.getRevind())) {
                cancleThirdRecords.add(item);
            } else {
                okThirdRecords.add(item);
            }
        }

        // 红包转入
        List<NewAssetLog> okLocalRecords = findAssetLog(userId, date, "7833");
        if (CollectionUtils.isEmpty(okLocalRecords)) {
            log.warn("[红包对账] 本地记录为空");
        }

        // 拨正流水
        List<NewAssetLog> cancelLocalRecords = findAssetLog(userId, date, "2833");
        if (CollectionUtils.isEmpty(cancelLocalRecords)) {
            log.warn("[红包对账] 本地拨正记录为空");
        }

        if (CollectionUtils.isEmpty(okThirdRecords)
                && CollectionUtils.isEmpty(okLocalRecords)) {
            log.warn("存管流水与本地流水相持平, 无需进行对账");
            return true;
        }

        doMatchOkRecord(userThirdAccount, date, okThirdRecords, okLocalRecords);

        if (CollectionUtils.isEmpty(cancleThirdRecords)
                && CollectionUtils.isEmpty(cancelLocalRecords)) {
            log.warn("存管拨正流水与本地拨正流水相持平, 无需进行对账");
            return true;
        }

        doMatchCancelRecord(userThirdAccount, date, cancleThirdRecords, cancelLocalRecords);
        log.info("红包记录匹配 结束");
        return false;
    }

    private void doMatchCancelRecord(UserThirdAccount userThirdAccount,
                                     Date date,
                                     List<NewAleve> cancleThirdRecords,
                                     List<NewAssetLog> cancelLocalRecords) throws Exception {
        log.info("[拨正红包对账] 红包金额匹配开始");
        Iterator<NewAleve> cancelThirdIterator = cancleThirdRecords.iterator();
        while (cancelThirdIterator.hasNext()) {
            NewAleve newAleve = cancelThirdIterator.next();
            Iterator<NewAssetLog> cancelLocalIterator = cancelLocalRecords.iterator();
            while (cancelLocalIterator.hasNext()) {
                NewAssetLog assetLog = cancelLocalIterator.next();
                long thirdMoney = MoneyHelper.yuanToFen(newAleve.getAmount());
                log.info(String.format("[拨正红包对账] 金额对比:存管变动金额  %s, 本地变动金额  %s", thirdMoney, assetLog.getOpMoney()));
                if (thirdMoney == assetLog.getOpMoney()) {
                    log.info(String.format("[拨正红包对账] 匹配成功 金额 %s", thirdMoney));
                    cancelThirdIterator.remove();
                    cancelLocalIterator.remove();
                    break;
                }
            }
        }

        // 存管派发信息多
        if (!CollectionUtils.isEmpty(cancleThirdRecords)) {
            log.warn("[拨正红包对账] 存管交易流水不为空, 进行补单操作");
            // 资金变动
            for (NewAleve item : cancleThirdRecords) {
                // 系统补单
                assetSubByErrorRecord(userThirdAccount, item);
            }
        }

        if (!CollectionUtils.isEmpty(cancelLocalRecords)) {
            log.warn("[拨正红包对账] 本地订单不为空, 进行拨正操作");
            for (NewAssetLog item : cancelLocalRecords) {
                // 系统拨正
                assetAddByErrorRecord(userThirdAccount, item);
            }
        }

        log.info("[拨正红包对账] 红包金额匹配开始");
    }


    /**
     * 诊断领取红包流水
     *
     * @param userThirdAccount 用户开户信息
     * @param date             对账时间
     * @param thirdRecords     存管红包流水
     * @param localRecords     本地红包流水
     * @return
     */
    private boolean doMatchOkRecord(UserThirdAccount userThirdAccount,
                                    Date date,
                                    List<NewAleve> thirdRecords,
                                    List<NewAssetLog> localRecords) throws Exception {
        log.info("[红包对账] 红包金额匹配开始");

        Iterator<NewAleve> okThirdIterator = thirdRecords.iterator();
        while (okThirdIterator.hasNext()) {
            NewAleve newAleve = okThirdIterator.next();
            Iterator<NewAssetLog> okLocalIterator = localRecords.iterator();
            while (okLocalIterator.hasNext()) {
                NewAssetLog assetLog = okLocalIterator.next();
                long thirdMoney = MoneyHelper.yuanToFen(newAleve.getAmount());
                log.info(String.format("[红包对账] 金额对比:存管变动金额  %s, 本地变动金额  %s", thirdMoney, assetLog.getOpMoney()));
                if (thirdMoney == assetLog.getOpMoney()) {
                    log.info(String.format("[红包对账] 匹配成功 金额 %s", thirdMoney));
                    okThirdIterator.remove();
                    okLocalIterator.remove();
                    break;
                }
            }
        }

        // 存管派发信息多
        if (!CollectionUtils.isEmpty(thirdRecords)) {
            log.warn("[红包对账] 存管交易流水不为空, 进行补单操作");
            // 资金变动
            for (NewAleve item : thirdRecords) {
                // 系统补单
                assetAddByOkRecord(userThirdAccount, item);
            }
        }


        if (!CollectionUtils.isEmpty(localRecords)) {
            log.warn("[红包对账] 本地订单不为空, 进行拨正操作");
            for (NewAssetLog item : localRecords) {
                assetSubByOkRecord(userThirdAccount, item);
            }
        }

        log.info("[红包对账] 红包金额匹配开始");
        return true;
    }

    /**
     * 系统拨正
     *
     * @param userThirdAccount
     * @param item
     * @throws Exception
     */
    private void assetSubByOkRecord(UserThirdAccount userThirdAccount, NewAssetLog item) throws Exception {
        log.info("[红包对账] 系统拨正开始");
        AssetChange assetChange = new AssetChange();
        assetChange.setMoney(item.getOpMoney());
        assetChange.setType(AssetChangeTypeEnum.redpackOkCancel);
        assetChange.setUserId(userThirdAccount.getUserId());
        assetChange.setRemark(String.format("系统拨正 %s元", MoneyHelper.divide(assetChange.getMoney(), 100, 2)));
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setForUserId(assetChangeProvider.getRedpackAccountId());
        assetChange.setSourceId(item.getSourceId());
        assetChangeProvider.commonAssetChange(assetChange);
        log.info("[红包对账] 系统拨正结束");
    }


    /**
     *  系统拨正流水的拨正
     * @param userThirdAccount
     * @param item
     * @throws Exception
     */
    private void assetAddByErrorRecord(UserThirdAccount userThirdAccount, NewAssetLog item) throws Exception {
        log.info("[红包对账] 系统拨正流水拨正开始");
        AssetChange assetChange = new AssetChange();
        assetChange.setMoney(item.getOpMoney());
        assetChange.setType(AssetChangeTypeEnum.redpackErrorCancel);
        assetChange.setUserId(userThirdAccount.getUserId());
        assetChange.setRemark(String.format("系统拨正 %s元", MoneyHelper.divide(assetChange.getMoney(), 100, 2)));
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setForUserId(assetChangeProvider.getRedpackAccountId());
        assetChange.setSourceId(item.getSourceId());
        assetChangeProvider.commonAssetChange(assetChange);
        log.info("[红包对账] 系统拨正流水拨正结束");
    }

    /**
     * 系统补单
     *
     * @param userThirdAccount
     * @param item
     * @throws Exception
     */
    private void assetAddByOkRecord(UserThirdAccount userThirdAccount, NewAleve item) throws Exception {
        log.info("[红包对账] 系统补单开始");
        long money = MoneyHelper.yuanToFen(item.getAmount());
        AssetChange assetChange = new AssetChange();
        assetChange.setMoney(money);
        assetChange.setType(AssetChangeTypeEnum.redpackOkRemedy);
        assetChange.setUserId(userThirdAccount.getUserId());
        assetChange.setRemark(String.format("系统补单 %s元", MoneyHelper.divide(money, 100, 2)));
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setForUserId(assetChangeProvider.getRedpackAccountId());
        assetChange.setSourceId(0L);
        assetChangeProvider.commonAssetChange(assetChange);
        log.info("[红包对账] 系统补单结束");
    }


    /**
     * 系统补单
     *
     * @param userThirdAccount
     * @param item
     * @throws Exception
     */
    private void assetSubByErrorRecord(UserThirdAccount userThirdAccount, NewAleve item) throws Exception {
        log.info("[红包对账] 系统拨正流水补单开始");
        long money = MoneyHelper.yuanToFen(item.getAmount());
        AssetChange assetChange = new AssetChange();
        assetChange.setMoney(money);
        assetChange.setType(AssetChangeTypeEnum.redpackErrorRemedy);
        assetChange.setUserId(userThirdAccount.getUserId());
        assetChange.setRemark(String.format("系统补单 %s元", MoneyHelper.divide(money, 100, 2)));
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setForUserId(assetChangeProvider.getRedpackAccountId());
        assetChange.setSourceId(0L);
        assetChangeProvider.commonAssetChange(assetChange);
        log.info("[红包对账] 系统拨正流水补单结束");
    }

    /**
     * 查找本地流水文件
     *
     * @param userId
     * @param date
     * @return
     */
    private List<NewAssetLog> findAssetLog(Long userId, Date date, @NotNull String transType) {
        Date beginDate = DateHelper.endOfDate(DateHelper.subDays(date, 1)); //  查询开始时间
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(date, 1)); // 查询结束时间
        Specification<NewAssetLog> newAssetLogSpecification = Specifications
                .<NewAssetLog>and()
                // 用户Id
                .eq("userId", userId)
                // 红包类型
                .eq("type", transType)
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
