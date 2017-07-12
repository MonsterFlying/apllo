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
    private AssetsChangeEnum type;

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
        onLineRecharge.setType(AssetsChangeEnum.OnlineRecharge) ;
        onLineRecharge.setName("在线充值") ;
        onLineRecharge.setAssetChangeRule("add@useMoney");
        onLineRecharge.setUserCacheChangeRule("add@rechargeTotal");
        onLineRecharge.setRemark("充值成功");
        assetChangeList.add(onLineRecharge) ;

        // 线下转账
        AssetsChangeConfig offLineRecharge =  new AssetsChangeConfig() ;
        offLineRecharge.setType(AssetsChangeEnum.offlineRecharge) ;
        offLineRecharge.setName("线下转账充值") ;
        offLineRecharge.setAssetChangeRule("add@useMoney");
        offLineRecharge.setUserCacheChangeRule("add@rechargeTotal");
        offLineRecharge.setRemark("充值成功");
        assetChangeList.add(offLineRecharge) ;

        // 银联提现
        AssetsChangeConfig smallCash =  new AssetsChangeConfig() ;
        smallCash.setType(AssetsChangeEnum.SmallCash) ;
        smallCash.setName("银联通道提现") ;
        smallCash.setAssetChangeRule("sub@noUseMoney");
        smallCash.setUserCacheChangeRule("add@cashTotal,add@expenditureFee#fee");
        smallCash.setRemark("提现成功") ;
        assetChangeList.add(smallCash) ;

        // 人行提现
        AssetsChangeConfig bigCash =  new AssetsChangeConfig() ;
        bigCash.setType(AssetsChangeEnum.SmallCash) ;
        bigCash.setName("人行通道提现") ;
        bigCash.setAssetChangeRule("sub@noUseMoney");
        bigCash.setUserCacheChangeRule("add@cashTotal,add@expenditureFee#fee");
        bigCash.setRemark("提现成功") ;
        assetChangeList.add(bigCash) ;

        // 冻结资金
        AssetsChangeConfig freeze =  new AssetsChangeConfig() ;
        freeze.setType(AssetsChangeEnum.Frozen) ;
        freeze.setName("冻结资金") ;
        freeze.setAssetChangeRule("sub@useMoney,add@noUseMoney");
        freeze.setRemark("冻结资金") ;
        assetChangeList.add(freeze) ;

        // 解冻资金
        AssetsChangeConfig unfreeze =  new AssetsChangeConfig() ;
        unfreeze.setType(AssetsChangeEnum.Unfrozen) ;
        unfreeze.setName("解冻资金") ;
        unfreeze.setAssetChangeRule("add@useMoney,sub@noUseMoney");
        unfreeze.setRemark("解冻资金") ;
        assetChangeList.add(unfreeze) ;

        // 正常还款
        AssetsChangeConfig repayment = new AssetsChangeConfig();
        repayment.setType(AssetsChangeEnum.Repayment);
        repayment.setName("还款");
        repayment.setAssetChangeRule("sub@useMoney");
        repayment.setUserCacheChangeRule("add@expenditureInterest#interest");
        repayment.setRemark("还款");
        assetChangeList.add(repayment);

        // 扣除待还
        AssetsChangeConfig paymentLower = new AssetsChangeConfig();
        paymentLower.setType(AssetsChangeEnum.PaymentLower);
        paymentLower.setName("扣除待还");
        paymentLower.setAssetChangeRule("sub@payment");
        paymentLower.setUserCacheChangeRule("sub@waitRepayPrincipal#principal,sub@waitRepayInterest#interest");
        paymentLower.setRemark("扣除待还");
        assetChangeList.add(paymentLower);

    }

    public static AssetsChangeConfig findAssetConfig(AssetsChangeEnum type) {
        List<AssetsChangeConfig> capitalChangeList = assetChangeList;
        for (AssetsChangeConfig config : capitalChangeList) {
            if ( (config.getType()!= null) && (config.getType().equals(type))) {
                return config;
            }
        }
        return null;
    }
}
