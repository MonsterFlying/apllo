package com.gofobao.framework.as.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.as.biz.AssetStatementBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MoneyHelper;
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

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public boolean checkUpAccount(@NotNull Date date) {
        // 查询指定时间, 资金存在变动的账户
        // 开始时间
        String beginDate = DateHelper.dateToString(DateHelper.endOfDate(DateHelper.subDays(date, 1)));
        String endDate = DateHelper.dateToString(DateHelper.beginOfDate(DateHelper.addDays(date, 1)));

        int pageSize = 100, pageIndex = 0;
        int pageIndexTatol = 0;
        Long count = newAssetLogService.countByDate(beginDate, endDate);
        if (count == 0) {
            log.warn("资金变动流水为空");
            return false;
        }
        pageIndexTatol = count.intValue() / pageSize;
        pageIndexTatol = count.intValue() % pageSize == 0 ? pageIndexTatol : pageIndexTatol + 1;
        do {
            log.info("数据库查询此处:  " + pageIndex);
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            List<NewAssetLog> assetLogList = newAssetLogService.findByDate(beginDate, endDate, pageable);
            Preconditions.checkNotNull(assetLogList, "assetPage record is empty");
            pageIndex++;
            Set<Long> userIds = assetLogList.stream().map(asset -> asset.getUserId()).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(userIds)) {
                log.warn("userids 为空!");
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
                threadPoolTaskExecutor.execute(new SearcheThred(jixinManager, asset, userThirdAccount));
            }
        } while (pageIndex < pageIndexTatol);
        return true;
    }
}

@Data
@Slf4j
class SearcheThred implements Runnable {
    private Gson gson = new Gson();

    private JixinManager jixinManager;

    private Asset asset;

    private UserThirdAccount userThirdAccount;

    public SearcheThred(JixinManager jixinManager, Asset asset, UserThirdAccount userThirdAccount) {
        this.jixinManager = jixinManager;
        this.asset = asset;
        this.userThirdAccount = userThirdAccount;
    }

    @Override
    public void run() {
        log.info("[启动查询用户资金线程]");
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
            BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
            if (JixinResultContants.isBusy(balanceQueryResponse)
                    || JixinResultContants.isNetWordError(balanceQueryResponse)) {
                continue;
            }
            if (JixinResultContants.SUCCESS.equals(balanceQueryResponse.getRetCode())) {
                // 账户总额
                String currBal = balanceQueryResponse.getCurrBal();
                // 账户可用
                String availBal = balanceQueryResponse.getAvailBal();
                long currBarFen = MoneyHelper.yuanToFen(currBal);
                long availBalFen = MoneyHelper.yuanToFen(availBal);
                long intavalMoney = asset.getNoUseMoney() + asset.getUseMoney() - currBarFen ;
                if(intavalMoney != 0){
                    log.info(String.format("相差金额[%s], 用户ID[%s], 手机[%s] 存管总金额[%s], 存管可用余额[%s], 平台资金总额[%s], 平台可用余额[%s] ",
                            intavalMoney,
                            userThirdAccount.getAccountId(),
                            userThirdAccount.getMobile(),
                            currBarFen,
                            availBalFen,
                            asset.getUseMoney() + asset.getNoUseMoney(),
                            asset.getUseMoney()));
                }

            } else {
                log.error(String.format("[用户资金查询] 资金查询失败 %s", gson.toJson(balanceQueryResponse)));
            }
        } while (looper < 1);
    }
}
