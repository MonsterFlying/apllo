package com.gofobao.framework.common.assets;


import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.entity.YesterdayAsset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.asset.service.YesterdayAssetService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
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
    NewAssetLogService newAssetLogService ;

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

                    DictValue dictValue = dictValueService.findTopByItemIdAndValue02(dictItem.getId(), jixinParamStr);
                    return ObjectUtils.isEmpty(dictValue) ? null : Long.parseLong(dictValue.getValue02());
                }
            });


    /**
     * 通用资金变动
     *
     * @param entity
     */
    public void commonAssetChange(AssetChange entity) throws Exception{
        Preconditions.checkNotNull(entity, "AssetChangeProvider.commonAssetChange assetEntity is null ");
        Preconditions.checkArgument(entity.getUserId() > 0, "AssetChangeProvider.commonAssetChange userId  <= 0");
        Preconditions.checkArgument(entity.getUserId() > 0, "AssetChangeProvider.commonAssetChange userId  <= 0");
        Preconditions.checkNotNull(entity.getType(), "AssetChangeProvider.commonAssetChange type is null");

        if (entity.getMoney() <= 0) {
            entity.setMoney(entity.getPrincipal() + entity.getInterest());
        }

        Preconditions.checkArgument(entity.getUserId() > 0,  "AssetChangeProvider.commonAssetChange moeny  <= 0");
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
        if(!StringUtils.isEmpty(assetChangeRule)) {  // 解析资金变动
            log.info(String.format("资金变动: 解析资金变动规则: %s", assetChangeRule));
            AssetChangeRuleParse.parse(asset, assetChangeRule, entity.getPrincipal(), entity.getInterest());
            asset.setUpdatedAt(nowDate);
            assetService.save(asset);
            // 查询资金变动记录
            NewAssetLog newAssetLog = new NewAssetLog() ;
            newAssetLog.setCreateTime(nowDate);
            newAssetLog.setUseMoney(asset.getUseMoney());
            newAssetLog.setCurrMoney(asset.getUseMoney() + asset.getNoUseMoney());
            newAssetLog.setDel(0);
            newAssetLog.setForUserId(entity.getForUserId());
            newAssetLog.setLocalSeqNo(entity.getSeqNo()) ;
            newAssetLog.setNoUseMoney(asset.getNoUseMoney());
            newAssetLog.setOpMoney(entity.getMoney());
            newAssetLog.setLocalType(entity.getType().getLocalType());
            newAssetLog.setPlatformType(entity.getType().getPlatformType());
            newAssetLog.setRemark(entity.getRemark());
            newAssetLog.setUserId(entity.getUserId());
            newAssetLog.setTxFlag(entity.getType().getTxFlag());
            newAssetLog.setGroupOpSeqNo(entity.getGroupSeqNo());
            newAssetLog.setOpName(entity.getType().getOpName());
            newAssetLogService.save(newAssetLog) ;
        }


        String userCacheChangeRule = entity.getType().getUserCacheChangeRule();
        if(!StringUtils.isEmpty(userCacheChangeRule)){
            log.info(String.format("资金变动: 解析资金缓存变动规则: %s", assetChangeRule));
            AssetChangeRuleParse.parse(userCache, userCacheChangeRule, entity.getPrincipal(), entity.getInterest()) ;
            userCache.setUpdatedAt(nowDate);
            userCache.setYesterdayUseMoney(yesterdayMoney > 0 ? yesterdayMoney : 0);
            userCacheService.save(userCache);
        }
    }

    /**
     * 获取费用账户Id
     * @return
     */
    public Long getFeeAccountId(){
        return jixinConfigCache.getIfPresent("handlingChargeUserId") ;
    }

    /**
     * 获取红包账户ID
     * @return
     */
    public Long getRedpackAccountId(){
        return jixinConfigCache.getIfPresent("redPacketUserId") ;
    }

    /**
     * 获取担保账户ID
     * @return
     */
    public Long getBailAccountId(){
        return jixinConfigCache.getIfPresent("bailUserId") ;
    }

    /**
     * 获取组的序列
     * @return
     */
    public String getGroupSeqNo(){
        return String.valueOf(System.currentTimeMillis()) ;
    }
}
