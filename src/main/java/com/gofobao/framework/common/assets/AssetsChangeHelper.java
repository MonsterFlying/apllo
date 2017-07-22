package com.gofobao.framework.common.assets;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.AssetsChangeLog;
import com.gofobao.framework.asset.entity.AssetsChangeLogItem;
import com.gofobao.framework.asset.entity.YesterdayAsset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.AssetsChangeLogItemService;
import com.gofobao.framework.asset.service.AssetsChangeLogService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/7/10 0010.
 */
@Component
public class AssetsChangeHelper {
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
    AssetsChangeLogService assetsChangeLogService;

    @Autowired
    AssetsChangeLogItemService assetsChangeLogItemService;

    @Autowired
    DictItemService dictItemService;

    @Autowired
    DictValueService dictValueService ;
    LoadingCache<String, Long> jixinConfigCache = CacheBuilder
            .newBuilder()
            .maximumSize(16)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Long>() {
                @Override
                public Long load(String jixinParamStr) throws Exception {
                    DictItem dictItem = dictItemService.findTopByAliasCodeAndDel("JIXIN_PARAM", 0) ;
                    if(ObjectUtils.isEmpty(dictItem)){
                        return null ;
                    }

                    DictValue dictValue = dictValueService.findTopByItemIdAndValue01(dictItem.getId(), jixinParamStr);
                    return ObjectUtils.isEmpty(dictValue) ? null : Long.parseLong(dictValue.getValue03()) ;
                }
            }) ;

    @Transactional(rollbackFor = Exception.class)
    public void execute(AssetsChangeEntity entity) throws Exception {
        Preconditions.checkNotNull(entity,
                "AssetsChangeHelper.execute assetEntity is null ");
        Preconditions.checkArgument(entity.getUserId() > 0,
                "AssetsChangeHelper.execute userId  <= 0");
        Preconditions.checkNotNull(entity.getType(),
                "AssetsChangeHelper.execute type is null");

        if (entity.getMoney() <= 0) {
            entity.setMoney(entity.getPrincipal() + entity.getInterest() );
        }
        if (entity.getPrincipal() <= 0) {
            entity.setPrincipal(entity.getMoney() - entity.getInterest());
        }
        AssetsChangeConfig assetsChangeConfig = AssetsChangeConfig.findAssetConfig(entity.getType());
        Preconditions.checkNotNull(assetsChangeConfig,
                "AssetsChangeHelper.execute assetsChangeConfig is null");


        long userId = entity.getUserId();
        Users user = userService.findByIdLock(userId);
        Preconditions.checkNotNull(user, "AssetsChangeHelper.execute user is null");
        if (user.getIsLock()) throw new Exception("AssetsChangeHelper.execute: 当前用户处于被锁定状态,");
        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "AssetsChangeHelper.execute asset is null");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "AssetsChangeHelper.execute userThirdAccount is null");
        UserCache userCache = userCacheService.findByUserIdLock(userId);
        Preconditions.checkNotNull(userCache, "AssetsChangeHelper.execute userCache is null");

        Date nowDate = new Date();
        Date zeroDate = DateHelper.beginOfDate(nowDate);
        // ==================================================================
        //  获取昨日资产
        // ==================================================================
        long yesterdayMoney = getYesterdayMoney(entity, userId, asset, nowDate, zeroDate);

        // ==================================================================
        //  资金变动
        // ==================================================================
        updateAsset(entity, assetsChangeConfig, userId, asset, userThirdAccount, nowDate);

        //==================================================================
        // 用户缓存资金变动
        //==================================================================
        updateUsesCache(entity, assetsChangeConfig, userCache, nowDate, yesterdayMoney);
    }

    private long getYesterdayMoney(AssetsChangeEntity entity, long userId, Asset asset, Date nowDate, Date zeroDate) {
        long yesterdayMoney =  0 ;
        if (asset.getUpdatedAt().getTime() < zeroDate.getTime()) {  // 写入昨日资产
            YesterdayAsset yesterdayAsset = yesterdayAssetService.findByUserId(userId);
            boolean initState = false;
            if (ObjectUtils.isEmpty(yesterdayAsset)) {
                yesterdayAsset = new YesterdayAsset();
                initState = true;
            }

            yesterdayMoney =  asset.getUseMoney();
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
        return yesterdayMoney;
    }

    private void updateAsset(AssetsChangeEntity entity, AssetsChangeConfig assetsChangeConfig, long userId, Asset asset, UserThirdAccount userThirdAccount, Date nowDate) throws Exception {
        if (!StringUtils.isEmpty(assetsChangeConfig.getAssetChangeRule())) {
            String assetChangeRule  = (StringUtils.isEmpty(entity.getAssetChangeRule())) ?  assetsChangeConfig.getAssetChangeRule() : entity.getAssetChangeRule() ;

            AssetsChangeRuleParse.parse(asset,
                    assetChangeRule,
                    entity.getPrincipal(),
                    entity.getInterest(),
                    entity.getFee());

            // 插入资金变换记录
            AssetsChangeLog changeLog = insetAssetChangeLog(assetsChangeConfig, userId, asset, userThirdAccount, nowDate, entity.getRemark());
            // 插入资金变动详细记录
            insertAssetChangeLogItem(entity, asset, userThirdAccount, nowDate, changeLog);
            asset.setUpdatedAt(nowDate);
            assetService.save(asset);
        }
    }

    private void insertAssetChangeLogItem(AssetsChangeEntity entity, Asset asset, UserThirdAccount userThirdAccount, Date nowDate, AssetsChangeLog changeLog) throws  Exception{
        Long changeLogId = changeLog.getId();
        // 记录日志详细
        AssetsChangeLogItem changeLogItem = new AssetsChangeLogItem();
        changeLogItem.setAssetsChangeLogId(changeLogId);
        changeLogItem.setUserId(entity.getUserId());
        changeLogItem.setAccountId(userThirdAccount.getAccountId());
        changeLogItem.setCreateAt(nowDate);
        changeLogItem.setUpdateAt(nowDate);
        changeLogItem.setMoney(entity.getMoney());
        changeLogItem.setRefId(entity.getRefId());
        changeLogItem.setThirdTxType(entity.getType().getTxType());
        changeLogItem.setCurrMoney(asset.getUseMoney() + asset.getNoUseMoney());
        changeLogItem.setAssetChangType(entity.getType().getType());
        long toUserId = entity.getToUserId();
        if (toUserId > 0) {   // To user
            changeLogItem.setToUserId(toUserId);
            UserThirdAccount toUserThirdAccount = userThirdAccountService.findByUserId(toUserId);
            Preconditions.checkNotNull(toUserThirdAccount, "AssetsChangeHelper.execute toUserThirdAccount is null") ;
            changeLogItem.setToUserAccountId(toUserThirdAccount.getAccountId());
        }
        assetsChangeLogItemService.save(changeLogItem);

        // 处理手续费
        if ((entity.getType().getFeeType() > 0) && (entity.getFee() >= 0)) {
            // 获取企业收费账户ID
            Long handlingChargeUserId  = null;
            handlingChargeUserId = jixinConfigCache.get("handlingChargeUserId");
            Preconditions.checkNotNull(handlingChargeUserId, "AssetsChangeHelper.execute handlingChargeUserId is null") ;
            UserThirdAccount handlingChargeUser = userThirdAccountService.findByUserId(handlingChargeUserId);
            Preconditions.checkNotNull(handlingChargeUser, "AssetsChangeHelper.execute handlingChargeUser is null") ;
            changeLogItem = new AssetsChangeLogItem();
            changeLogItem.setAssetsChangeLogId(changeLogId);
            changeLogItem.setUserId(entity.getUserId());
            changeLogItem.setAccountId(userThirdAccount.getAccountId());
            changeLogItem.setCreateAt(nowDate);
            changeLogItem.setUpdateAt(nowDate);
            changeLogItem.setMoney(entity.getFee());
            changeLogItem.setRefId(entity.getRefId());
            changeLogItem.setThirdTxType(entity.getType().getFeeTxType());
            changeLogItem.setCurrMoney(asset.getUseMoney() + asset.getNoUseMoney());
            changeLogItem.setAssetChangType(entity.getType().getFeeType());
            changeLogItem.setToUserId(toUserId);
            changeLogItem.setToUserAccountId(handlingChargeUser.getAccountId());
            assetsChangeLogItemService.save(changeLogItem);
        }
    }

    private AssetsChangeLog insetAssetChangeLog(AssetsChangeConfig assetsChangeConfig, long userId, Asset asset, UserThirdAccount userThirdAccount, Date nowDate, String remark) {
        // 记录日志
        AssetsChangeLog changeLog = new AssetsChangeLog();
        changeLog.setUpdateAt(nowDate);
        changeLog.setCreateAt(nowDate);
        changeLog.setUseMoney(asset.getUseMoney());
        changeLog.setNoUseMoney(asset.getNoUseMoney());
        changeLog.setCollection(asset.getCollection());
        changeLog.setPayment(asset.getPayment());
        changeLog.setVirtualMoney(asset.getVirtualMoney());
        changeLog.setUserId(userId);
        changeLog.setAccountId(userThirdAccount.getAccountId());
        changeLog.setAssetChangeType(assetsChangeConfig.getType().getType());
        changeLog.setMoney(asset.getUseMoney() + asset.getNoUseMoney());
        changeLog.setSynState(0); // 未同步
        changeLog.setRemark(StringUtils.isEmpty(remark)? assetsChangeConfig.getRemark() : remark);
        changeLog = assetsChangeLogService.save(changeLog);
        return changeLog;
    }

    /**
     * 更新用户缓存资产
     *
     * @param entity
     * @param assetsChangeConfig
     * @param userCache
     * @param nowDate
     * @param yesterdayMoney
     * @throws Exception
     */
    private void updateUsesCache(AssetsChangeEntity entity, AssetsChangeConfig assetsChangeConfig, UserCache userCache, Date nowDate, long yesterdayMoney) throws Exception {
        if (!StringUtils.isEmpty(assetsChangeConfig.getUserCacheChangeRule())) {
            AssetsChangeRuleParse.parse(userCache,
                    assetsChangeConfig.getUserCacheChangeRule(),
                    entity.getPrincipal(),
                    entity.getInterest(),
                    entity.getFee());
        }
        userCache.setUpdatedAt(nowDate);
        userCache.setYesterdayUseMoney(yesterdayMoney > 0 ? yesterdayMoney : 0);
        userCacheService.save(userCache);
    }
}
