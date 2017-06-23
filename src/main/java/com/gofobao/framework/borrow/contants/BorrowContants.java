package com.gofobao.framework.borrow.contants;

/**
 * Created by admin on 2017/5/17.
 */
public class BorrowContants {

    /**
     * 车贷标
     */
    public static final int CE_DAI = 0;
    /**
     * 净值标
     */
    public static final int JING_ZHI = 1;

    /**
     *秒标
     */
    public static final int MIAO_BIAO = 2;

    /**
     * 渠道标
     */
    public static final int QU_DAO = 4;


    /**
     * 发标待审
     */
    public static final Integer PENDING = 0;

    /**
     * 招标中；
     */
    public static final Integer BIDDING = 1;


    /**
     * 初审不通过；
     */
    public static final Integer NO_PASS = 2;

    /**
     * 满标复审通过
     */
    public static final Integer PASS = 3;

    /**
     * 复审不通过
     */
    public static final Integer RECHECK_NO_PASS = 4;

    /**
     * 已取消；
     */
    public static final Integer CANCEL = 5;


    /**
     * 按月分期
     */
    public static final Integer REPAY_FASHION_MONTH = 0;


    public static final String REPAY_FASHION_MONTH_STR = "按月分期";


    /**
     * 一次性还本付息
     */
    public static final Integer REPAY_FASHION_ONCE = 1;


    public static final String REPAY_FASHION_ONCE_STR = "一次性还本付息";


    /**
     * 先息后本
     */
    public static final Integer REPAY_FASHION_INTEREST_THEN_PRINCIPAL = 2;


    public static final String REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR = "先息后本";


    public static final String MONTH = "个月";


    public static final String DAY = "天";

    public static final String TIME = "次";

    public static final String  PERCENT="%";

    /**
     * 车贷标
     */
    public static final int INDEX_TYPE_CE_DAI = 1;
    /**
     * 净值标
     */
    public static final int INDEX_TYPE_JING_ZHI = 2;

    /**
     * 渠道标
     */
    public static final int INDEX_TYPE_QU_DAO = 4;
    /**
     * 转让标
     */
    public static final int INDEX_TYPE_LIU_ZHUAN = 5;



}
