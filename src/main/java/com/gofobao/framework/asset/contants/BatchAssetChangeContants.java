package com.gofobao.framework.asset.contants;

/**
 * Created by Zeke on 2017/8/2.
 */
public class BatchAssetChangeContants {
    public static final int BATCH_LEND_REPAY = 1;/* 批次放款 */
    public static final int BATCH_REPAY = 2;/* 批次还款 */
    /*public static final int BATCH_REPAY_BAIL = 3; 批次融资人还担保账户垫款 */ /*弃用*/
    public static final int BATCH_CREDIT_INVEST = 4;/* 批次投资人购买债权 */
    public static final int BATCH_BAIL_REPAY = 5;/* 批次名义借款人垫付 */
    public static final int BATCH_REPAY_ALL = 6;/* 批次提前结清 */
    public static final int BATCH_FINANCE_CREDIT_INVEST = 7; /*理财计划批次购买债权 */
    public static final int BATCH_FINANCE_LEND_REPAY = 8;/* 理财计划批次放款 */
    public static final int BATCH_REPURCHASE_FINANCE_CREDIT_INVEST = 7; /*赎回理财计划批次购买债权 */
}
