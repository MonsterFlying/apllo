package com.gofobao.framework.common.assets;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/8 0008.
 */
@Data
public class AssetsChangeConfig {
    /**  资金变动类型 */
    private AssetChangeEnum type;

    /**  资金变动名称 */
    private String name;

    /** 资金变动规则 */
    private String assetChangeRule;

    /**  用户缓存资金变动规则 */
    private String userCacheChangeRule;

    /**  备注  */
    private String remark;

    /**
     * 规则类
     */
    public final static List<AssetsChangeConfig> assetChangeList = new ArrayList<>();


    static {
        // 在线充值
        AssetsChangeConfig onLineRecharge =  new AssetsChangeConfig() ;
        onLineRecharge.setType(AssetChangeEnum.OnlineRecharge) ;
        onLineRecharge.setName("在线充值") ;
        onLineRecharge.setAssetChangeRule("add@useMoney");
        onLineRecharge.setUserCacheChangeRule("add@rechargeTotal");
        onLineRecharge.setRemark("充值成功");
        assetChangeList.add(onLineRecharge) ;

        // 线下转账
        AssetsChangeConfig offLineRecharge =  new AssetsChangeConfig() ;
        offLineRecharge.setType(AssetChangeEnum.offlineRecharge) ;
        offLineRecharge.setName("线下转账充值") ;
        offLineRecharge.setAssetChangeRule("add@useMoney");
        offLineRecharge.setUserCacheChangeRule("add@rechargeTotal");
        offLineRecharge.setRemark("充值成功");
        assetChangeList.add(offLineRecharge) ;

        // 银联提现
        AssetsChangeConfig smallCash =  new AssetsChangeConfig() ;
        smallCash.setType(AssetChangeEnum.SmallCash) ;
        smallCash.setName("银联通道提现") ;
        smallCash.setAssetChangeRule("sub@noUseMoney");
        smallCash.setUserCacheChangeRule("add@cashTotal,add@expenditureFee#fee");
        smallCash.setRemark("提现成功") ;
        assetChangeList.add(offLineRecharge) ;
    }

    public static AssetsChangeConfig findAssetConfig(AssetChangeEnum type) {
        List<AssetsChangeConfig> capitalChangeList = assetChangeList;
        for (AssetsChangeConfig config : capitalChangeList) {
            if ( (config.getType()!= null) && (config.getType().equals(type))) {
                return config;
            }
        }
        return null;
    }
}
