package com.gofobao.framework.borrow.contants;

/**
 * 还款方式
 * Created by Max on 17/3/7.
 */
public class RepaymentContants {
    /**
     * 还款方式；0：按月分期；1：一次性还本付息；2：先息后本；
     */
    /**
     * 按月分期
     */
    public static final int AN_YUE_FEN_QI = 0;

    /**
     * 一次性还本付息
     */
    public static final int YI_CI_XING_HUAN_BEN_FU_XI = 1;

    /**
     * 先息后本
     */
    public static final int XIAN_XI_HOU_BEN = 2;

    /**
     * 还款方式集合
     */
    public static final int[] ALLS = {AN_YUE_FEN_QI, YI_CI_XING_HUAN_BEN_FU_XI, XIAN_XI_HOU_BEN};

}
