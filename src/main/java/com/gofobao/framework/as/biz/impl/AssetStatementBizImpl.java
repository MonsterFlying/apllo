package com.gofobao.framework.as.biz.impl;

import com.github.wenhao.jpa.PredicateBuilder;
import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.as.biz.AssetStatementBiz;
import com.gofobao.framework.as.entity.RealtimeAsset;
import com.gofobao.framework.as.service.RealtimeAssetService;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Component
@Slf4j
public class AssetStatementBizImpl implements AssetStatementBiz {
    @Autowired
    AssetService assetService;

    @Autowired
    NewAssetLogService newAssetLogService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    RealtimeAssetService realtimeAssetService;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Override
    public boolean checkUpAccountForChange() {
        log.info("============================");
        log.info("根据开户信息查询开户记录");
        log.info("============================");
        long batchNo = System.currentTimeMillis();
        Date date = jixinTxDateHelper.getSubDate(1);
        String beginDate = DateHelper.dateToString(DateHelper.endOfDate(DateHelper.subDays(date, 1)));
        String endDate = DateHelper.dateToString(DateHelper.beginOfDate(DateHelper.addDays(date, 1)));

        int pageSize = 100, pageIndex = 0;
        int pageIndexTatol = 0;
        Long count = newAssetLogService.countByDate(beginDate, endDate);
        if (count == 0) {
            log.warn("[用户资金记录查询] 待查询记录为零");
            return false;
        }
        pageIndexTatol = count.intValue() / pageSize;
        pageIndexTatol = count.intValue() % pageSize == 0 ? pageIndexTatol : pageIndexTatol + 1;
        log.info(String.format("[用户资金记录查询] 待检测总数: %s", pageIndexTatol));
        do {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            List<NewAssetLog> assetLogList = newAssetLogService.findByDate(beginDate, endDate, pageable);
            Preconditions.checkNotNull(assetLogList, "assetPage record is empty");
            pageIndex++;
            Set<Long> userIds = assetLogList.stream().map(asset -> asset.getUserId()).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(userIds)) {
                log.warn("[用户资金记录查询] userIds集合为空!");
                break;
            }

            Specification<UserThirdAccount> userThirdAccountSpecification = Specifications
                    .<UserThirdAccount>and()
                    .in("userId", userIds.toArray())
                    .build();
            List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(userThirdAccountSpecification);
            Preconditions.checkNotNull(userThirdAccountList, "userThirdAccountList record is empty");
            Map<Long, UserThirdAccount> userThirdAccountMap = userThirdAccountList.stream().collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));

            Specification<Asset> assetSpecification = Specifications
                    .<Asset>and()
                    .in("userId", userIds.toArray())
                    .build();
            List<Asset> assetList = assetService.findList(assetSpecification);
            Preconditions.checkNotNull(assetList, "assetList reocrd is empty");
            Map<Long, Asset> assetMap = assetList.stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));
            Long userId = null;

            for (NewAssetLog item : assetLogList) {
                userId = item.getUserId();
                Asset asset = assetMap.get(userId);
                UserThirdAccount userThirdAccount = userThirdAccountMap.get(userId);
                threadPoolTaskExecutor.execute(new SearcheThred(jixinManager, asset, userThirdAccount, realtimeAssetService, batchNo));
            }
        } while (pageIndex < pageIndexTatol);
        return true;
    }


    @Override
    public boolean checkUpAccountForAll() {
        log.info("============================");
        log.info("根据开户信息查询开户记录");
        log.info("============================");
        return doCheckUpAccount(false);
    }

    private boolean doCheckUpAccount(boolean activeState) {
        long batchNo = System.currentTimeMillis();
        int pageSize = 100, pageIndex = 0;
        int pageIndexTatol = 0;
        PredicateBuilder<UserThirdAccount> predicateBuilder = Specifications.<UserThirdAccount>and();
        if (activeState) {
            // 查询活跃用户
            predicateBuilder.eq("activeState", 1);
        }

        Specification<UserThirdAccount> userThirdAccountSpecificationForCount =
                predicateBuilder.build();
        Long count = userThirdAccountService.count(userThirdAccountSpecificationForCount);
        if (count == 0) {
            log.warn("[用户资金记录查询] 待查询记录为零");
            return false;
        }
        pageIndexTatol = count.intValue() / pageSize;
        pageIndexTatol = count.intValue() % pageSize == 0 ? pageIndexTatol : pageIndexTatol + 1;
        log.info(String.format("[用户资金记录查询] 待检测总数: %s", pageIndexTatol));
        do {
            log.info("================================");
            log.info("账户查询进度" + (MoneyHelper.multiply(MoneyHelper.divide(pageIndex, pageIndexTatol, 4), 100, 2)) + "%");
            log.info("================================");
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            Specification<UserThirdAccount> userThirdAccountSpecification = Specifications
                    .<UserThirdAccount>and()
                    .build();
            List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(userThirdAccountSpecification, pageable);
            Preconditions.checkNotNull(userThirdAccountList, "userThirdAccountList record is empty");
            pageIndex++;

            Set<Long> userIds = userThirdAccountList.stream().map(userThirdAccount -> userThirdAccount.getUserId()).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(userIds)) {
                log.warn("[用户资金记录查询] userIds集合为空!");
                break;
            }

            Specification<Asset> assetSpecification = Specifications
                    .<Asset>and()
                    .in("userId", userIds.toArray())
                    .build();
            List<Asset> assetList = assetService.findList(assetSpecification);
            Preconditions.checkNotNull(assetList, "assetList reocrd is empty");
            Map<Long, Asset> assetMap = assetList.stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));
            Long userId = null;

            for (UserThirdAccount userThirdAccount : userThirdAccountList) {
                userId = userThirdAccount.getUserId();
                Asset asset = assetMap.get(userId);
                Preconditions.checkNotNull(asset, "asset record is empty");
                threadPoolTaskExecutor.execute(new SearcheThred(jixinManager, asset, userThirdAccount, realtimeAssetService, batchNo));
            }

        } while (pageIndex < pageIndexTatol);
        return true;
    }

    @Override
    public boolean checkUpAccountForActiveState() {
        log.info("============================");
        log.info("根据用户活跃度查询账户资金");
        log.info("============================");
        doCheckUpAccount(true) ;
        return false;
    }

}

@Data
@Slf4j
class SearcheThred implements Runnable {
    private Gson gson = new Gson();

    private JixinManager jixinManager;

    private Asset asset;

    private UserThirdAccount userThirdAccount;

    private RealtimeAssetService realtimeAssetService;

    private long batchNo;

    public SearcheThred(JixinManager jixinManager,
                        Asset asset,
                        UserThirdAccount userThirdAccount,
                        RealtimeAssetService realtimeAssetService,
                        Long btachNo) {
        this.jixinManager = jixinManager;
        this.asset = asset;
        this.userThirdAccount = userThirdAccount;
        this.realtimeAssetService = realtimeAssetService;
        this.batchNo = btachNo;
    }

    @Override
    public void run() {
        // 查询资金变动
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        int looper = 5;
        do {
            --looper;
            balanceQueryRequest.setTxDate(null);
            balanceQueryRequest.setTxTime(null);
            balanceQueryRequest.setSeqNo(null);
            // 不带log日志查询
            BalanceQueryResponse balanceQueryResponse = jixinManager.baseSend(JixinTxCodeEnum.BALANCE_QUERY,
                    balanceQueryRequest,
                    BalanceQueryResponse.class,
                    false);

            if (JixinResultContants.isBusy(balanceQueryResponse)
                    || JixinResultContants.isNetWordError(balanceQueryResponse)) {
                continue;
            }

            // 查询成功
            if (!ObjectUtils.isEmpty(balanceQueryResponse)
                    && JixinResultContants.SUCCESS.equals(balanceQueryResponse.getRetCode())) {
                // 账户总额
                String currBal = balanceQueryResponse.getCurrBal();
                // 账户可用
                String availBal = balanceQueryResponse.getAvailBal();
                long currBarFen = MoneyHelper.yuanToFen(currBal);
                long intavalMoney = currBarFen - (asset.getNoUseMoney() + asset.getUseMoney());

                if (intavalMoney != 0) {
                    RealtimeAsset realtimeAsset = new RealtimeAsset();
                    realtimeAsset.setAccountId(userThirdAccount.getId());
                    realtimeAsset.setBatchNo(batchNo);
                    realtimeAsset.setCreateTime(new Date());
                    realtimeAsset.setJixinTotalAmount(new BigDecimal(currBal).doubleValue());
                    realtimeAsset.setJixinUseAmount(new BigDecimal(availBal).doubleValue());
                    realtimeAsset.setLocalTotalAmount(MoneyHelper.divide(asset.getNoUseMoney() + asset.getUseMoney(), 100, 2));
                    realtimeAsset.setLocalUseAmount(MoneyHelper.divide(asset.getUseMoney(), 100, 2));
                    realtimeAsset.setIntevalMoney(MoneyHelper.divide(intavalMoney, 100, 2));
                    realtimeAsset.setPhone(userThirdAccount.getMobile());
                    realtimeAsset.setUsername(userThirdAccount.getName());
                    realtimeAsset.setUserId(userThirdAccount.getUserId());
                    realtimeAsset = realtimeAssetService.save(realtimeAsset);
                }

            } else {
                log.error(String.format("[用户资金查询] 资金查询失败 %s", gson.toJson(balanceQueryResponse)));
            }
        } while (looper < 1);
    }
}
