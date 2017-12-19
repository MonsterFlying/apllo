package com.gofobao.framework.common.assets;


import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.entity.YesterdayAsset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.asset.service.YesterdayAssetService;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class AssetChangeProvider {
    @Autowired
    UserService userService;

    @Autowired
    AssetService assetService;

    @Autowired
    UserCacheService userCacheService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    YesterdayAssetService yesterdayAssetService;


    @Autowired
    DictItemService dictItemService;

    @Autowired
    DictValueService dictValueService;

    @Autowired
    NewAssetLogService newAssetLogService;

    /**
     * 即信参数
     */
    LoadingCache<String, Long> jixinConfigCache = CacheBuilder
            .newBuilder()
            .maximumSize(16)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Long>() {
                @Override
                public Long load(String jixinParamStr) throws Exception {
                    DictItem dictItem = dictItemService.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    DictValue dictValue = dictValueService.findTopByItemIdAndValue01(dictItem.getId(), jixinParamStr);
                    return ObjectUtils.isEmpty(dictValue) ? null : Long.parseLong(dictValue.getValue03());
                }
            });


    /**
     * 通用资金变动
     * @// TODO: 2017/12/19 奖励金  增加前置奖励金转换 
     *
     * @param entity
     */
    public void commonAssetChange(AssetChange entity) throws Exception {
        try {
            log.info(String.format("资金变动规则处理前：%s", new Gson().toJson(entity)));
            Preconditions.checkNotNull(entity, "AssetChangeProvider.commonAssetChange assetEntity is null ");
            Preconditions.checkArgument(entity.getUserId() > 0, "AssetChangeProvider.commonAssetChange userId  <= 0");
            Preconditions.checkNotNull(entity.getType(), "AssetChangeProvider.commonAssetChange type is null");

            if (entity.getMoney() <= 0) {
                entity.setMoney(entity.getPrincipal() + entity.getInterest());
            }

            Preconditions.checkArgument(entity.getUserId() > 0, "AssetChangeProvider.commonAssetChange moeny  <= 0");
            if (entity.getPrincipal() <= 0) {
                entity.setPrincipal(entity.getMoney() - entity.getInterest());
            }

            long userId = entity.getUserId();
            Users user = userService.findByIdLock(userId);
            Preconditions.checkNotNull(user, "AssetsChangeHelper.execute user is null");

            Asset asset = assetService.findByUserIdLock(userId);
            Preconditions.checkNotNull(asset, "AssetsChangeHelper.execute asset is null");

            UserCache userCache = userCacheService.findByUserIdLock(userId);
            Preconditions.checkNotNull(userCache, "AssetsChangeHelper.execute userCache is null");

            Date nowDate = new Date();
            Date zeroDate = DateHelper.beginOfDate(nowDate);
            long yesterdayMoney = 0L; // 昨日资产
            if (asset.getUpdatedAt().getTime() < zeroDate.getTime()) {  // 写入昨日资产
                log.info("资金变动: 更新用户昨日缓存");
                YesterdayAsset yesterdayAsset = yesterdayAssetService.findByUserId(userId);
                boolean initState = false;
                if (ObjectUtils.isEmpty(yesterdayAsset)) {
                    yesterdayAsset = new YesterdayAsset();
                    initState = true;
                }

                yesterdayMoney = asset.getUseMoney();
                yesterdayAsset.setUseMoney(asset.getUseMoney());
                yesterdayAsset.setNoUseMoney(asset.getNoUseMoney());
                yesterdayAsset.setUseMoney(asset.getUseMoney());
                yesterdayAsset.setNoUseMoney(asset.getNoUseMoney());
                yesterdayAsset.setVirtualMoney(asset.getVirtualMoney());
                yesterdayAsset.setCollection(asset.getCollection());
                yesterdayAsset.setPayment(asset.getPayment());
                yesterdayAsset.setUpdatedAt(nowDate);
                yesterdayAsset.setUserId(entity.getUserId());
                if (initState) {
                    yesterdayAssetService.insert(yesterdayAsset);
                } else {
                    yesterdayAssetService.update(yesterdayAsset);
                }
            }

            String assetChangeRule = entity.getType().getAssetChangeRule();
            if (!StringUtils.isEmpty(assetChangeRule)) {  // 解析资金变动
                log.info(String.format("资金变动: 解析资金变动规则: %s", assetChangeRule));
                AssetChangeRuleParse.parse(asset, assetChangeRule, entity.getPrincipal(), entity.getInterest());
                asset.setUpdatedAt(nowDate);
                assetService.save(asset);
                // 查询资金变动记录
                NewAssetLog newAssetLog = new NewAssetLog();
                newAssetLog.setCreateTime(nowDate);
                newAssetLog.setUseMoney(asset.getUseMoney());
                newAssetLog.setCurrMoney(asset.getUseMoney() + asset.getNoUseMoney());
                newAssetLog.setDel(0);
                if (!ObjectUtils.isEmpty(entity.getForUserId())) {
                    newAssetLog.setForUserId(entity.getForUserId());
                }
                newAssetLog.setLocalSeqNo(entity.getSeqNo());
                newAssetLog.setType(entity.getAssetType());
                newAssetLog.setNoUseMoney(asset.getNoUseMoney());
                newAssetLog.setOpMoney(entity.getMoney());
                newAssetLog.setLocalType(entity.getType().getLocalType());
                newAssetLog.setPlatformType(entity.getType().getPlatformType());
                newAssetLog.setRemark(entity.getRemark());
                newAssetLog.setUserId(entity.getUserId());
                newAssetLog.setTxFlag(entity.getType().getTxFlag());
                newAssetLog.setGroupOpSeqNo(entity.getGroupSeqNo());
                newAssetLog.setOpName(entity.getType().getOpName());
                if (!ObjectUtils.isEmpty(entity.getSourceId())) {
                    newAssetLog.setSourceId(entity.getSourceId());
                }
                newAssetLogService.save(newAssetLog);
            }


            log.info(String.format("资金变动规则处理后：%s", new Gson().toJson(entity)));
            String userCacheChangeRule = entity.getType().getUserCacheChangeRule();
            if (!StringUtils.isEmpty(userCacheChangeRule)) {
                log.info(String.format("资金变动: 解析资金缓存变动规则: %s", assetChangeRule));
                AssetChangeRuleParse.parse(userCache, userCacheChangeRule, entity.getPrincipal(), entity.getInterest());
                userCache.setUpdatedAt(nowDate);
                userCache.setYesterdayUseMoney(yesterdayMoney > 0 ? yesterdayMoney : 0);
                userCacheService.save(userCache);
            }
        } catch (Exception e) {
            log.error(String.format("资金变动规则处理失败：%s", new Gson().toJson(entity)));
            log.error("error:", e);
            throw new Exception(e);
        }
    }

    /**
     * 获取费用账户Id
     *
     * @return
     */
    public Long getFeeAccountId() throws ExecutionException {
        return jixinConfigCache.get("handlingChargeUserId");
    }

    /**
     * 获取红包账户ID
     *
     * @return
     */
    public Long getRedpackAccountId() throws ExecutionException {
        return jixinConfigCache.get("redPacketUserId");
    }


    /**
     * 获取名义借款人id
     *
     * @return
     */
    public Long getTitularBorrowUserId() throws ExecutionException {
        return jixinConfigCache.get("titularBorrowUserId");
    }

    /**
     * 获取组的序列
     *
     * @return
     */
    public String getGroupSeqNo() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 获取标准序列号
     *
     * @return
     */
    public String getSeqNo() {
        return String.format("%s%s", DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMDHMS_NUM), RandomHelper.generateNumberCode(6));
    }
}
