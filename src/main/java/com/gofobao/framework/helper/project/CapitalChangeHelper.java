package com.gofobao.framework.helper.project;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.entity.YesterdayAsset;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.YesterdayAssetService;
import com.gofobao.framework.common.capital.CapitalChangeConfig;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.capital.CapitalChangeRulePaser;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 资金变动帮助类
 * Created by Max on 17/3/10.
 */
@Component
public class CapitalChangeHelper {
    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetLogService assetLogService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private YesterdayAssetService yesterdayAssetService;


    @Transactional(rollbackFor = Exception.class)
    public boolean capitalChange(CapitalChangeEntity entity) throws Exception {
        if (entity.getUserId() <= 0) {
            return false;
        }

        if (entity.getMoney() <= 0) {
            entity.setMoney(entity.getPrincipal() + entity.getInterest());
        }

        if (entity.getMoney() <= 0) {
            return false;
        }

        if (entity.getPrincipal() <= 0) {
            entity.setPrincipal(entity.getMoney() - entity.getInterest());
        }
        if (entity.getType() == null) {
            return false;
        }


        CapitalChangeConfig capitalChangeConfig = findCapitalChangeConfig(entity.getType());

        if (capitalChangeConfig == null) {
            return false;
        }

        String assetStr = entity.getAsset();
        if (!ObjectUtils.isEmpty(assetStr)) {
            capitalChangeConfig.setAssetChangeRule(assetStr);
        }

        long userId = entity.getUserId();
        Asset asset = assetService.findByUserIdLock(userId);
        UserCache userCache = userCacheService.findByUserIdLock(userId);

        // 更新昨日资产
        Date zeroDate = DateHelper.beginOfDate(new Date());
        int yesterdayMoney = 0; //  昨日资产
        if (asset.getUpdatedAt().getTime() < zeroDate.getTime()) { //  写入昨日资产
            YesterdayAsset yesterdayAsset = yesterdayAssetService.findByUserId(userId);
            boolean isInsert = false;
            if (yesterdayAsset == null) {
                yesterdayAsset = new YesterdayAsset();
                isInsert = true;
            }
            yesterdayMoney = asset.getUseMoney();
            yesterdayAsset.setUseMoney(asset.getUseMoney());
            yesterdayAsset.setNoUseMoney(asset.getNoUseMoney());
            yesterdayAsset.setVirtualMoney(asset.getVirtualMoney());
            yesterdayAsset.setCollection(asset.getCollection());
            yesterdayAsset.setPayment(asset.getPayment());
            yesterdayAsset.setUpdatedAt(new Date());
            yesterdayAsset.setUserId(entity.getUserId());
            if (isInsert) {
               yesterdayAssetService.insert(yesterdayAsset);
            } else {
               yesterdayAssetService.update(yesterdayAsset);
            }
        }

        boolean flag;

        if (!StringUtils.isEmpty(capitalChangeConfig.getAssetChangeRule())) {  // 用户资金变动
            flag = CapitalChangeRulePaser.paser(asset, capitalChangeConfig.getAssetChangeRule(), entity.getPrincipal(), entity.getInterest());
            if (!flag) {
                return false;
            }

            String remark = StringUtils.isEmpty(entity.getRemark()) ? capitalChangeConfig.getRemark() : entity.getRemark();
            AssetLog assetLog = new AssetLog();
            assetLog.setUserId(userId);
            assetLog.setToUserId(entity.getToUserId());
            assetLog.setType(capitalChangeConfig.getType().getValue());
            assetLog.setMoney(entity.getMoney());
            assetLog.setUseMoney(asset.getUseMoney());
            assetLog.setNoUseMoney(asset.getNoUseMoney());
            assetLog.setVirtualMoney(asset.getVirtualMoney());
            assetLog.setCollection(asset.getCollection());
            assetLog.setPayment(asset.getPayment());
            assetLog.setCreatedAt(new Date());
            assetLog.setRemark(remark);
            assetLogService.insert(assetLog);
            asset.setUpdatedAt(new Date());
            assetService.updateById(asset);
        }


        if (!StringUtils.isEmpty(capitalChangeConfig.getUserCacheChangeRule())) { // 用户缓存资金变动
            flag = CapitalChangeRulePaser.paser(userCache, capitalChangeConfig.getUserCacheChangeRule(), entity.getPrincipal(), entity.getInterest());
            if (!flag) {
                throw new Exception("解析用户资金缓存失败");
            }

            userCache.setUpdatedAt(new Date());
            if (yesterdayMoney != 0) {
                userCache.setYesterdayUseMoney(yesterdayMoney > 0 ? yesterdayMoney : 0);
            }
            userCacheService.updateById(userCache);
        } else {
            if (yesterdayMoney != 0) {
                userCache.setYesterdayUseMoney(yesterdayMoney);
            }
           userCacheService.updateById(userCache);
        }

        return true;
    }

    /**
     * 查找资金变动记录表
     *
     * @param capitalChangeEnum
     * @return
     */
    private CapitalChangeConfig findCapitalChangeConfig(CapitalChangeEnum capitalChangeEnum) {
        List<CapitalChangeConfig> capitalChangeList = CapitalChangeConfig.capitalChangeList;
        for (CapitalChangeConfig config : capitalChangeList) {
            if (config.getType()!=null&&config.getType().equals(capitalChangeEnum)) {
                return config;
            }
        }
        return null;
    }
}
