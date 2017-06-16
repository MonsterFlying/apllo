package com.gofobao.framework.borrow.contants;

/**
 * Created by Zeke on 2017/5/26.
 */
public class BorrowVerifyContants {
    /**
     * 借款期限月最小值
     */
    public static final int MOUTH_MIN = 1;

    /**
     * 借款期限月最大值
     */
    public static final int MOUTH_MAX = 36;
    /**
     * 借款期限日最小值
     */
    public static final int DAY_MIN = 1;

    /**
     * 借款期限日最大值
     */
    public static final int DAY_MAX = 92;


    /**
     * 借款最小金额
     */
    public static final int MONEY_MIN = 500;

    /**
     * 借款最大金额
     */
    public static final int MONEY_MAX = 100000;

    /**
     * 最小年化率
     */
    public static final int APR_MIN = 5 * 100;

    /**
     * 最大年化率
     */
    public static final int APR_MAX = 24 * 100;

    /**
     * 最小投标期限
     */
    public static final int VALID_APR_MIN = 1;

    /**
     * 最大投标期限
     */
    public static final int VALID_APR_MAX = 3;
}
