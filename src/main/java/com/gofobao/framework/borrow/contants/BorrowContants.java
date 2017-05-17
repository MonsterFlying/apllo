package com.gofobao.framework.borrow.contants;

/**
 * Created by admin on 2017/5/17.
 */
public class BorrowContants {

    /**
     * 发标待审
     */
    public static final Integer PENDING=0;

    /**
     * 招标中；
     */
    public static final Integer BIDDING=1;


    /**
     * 初审不通过；
     */
    public static final Integer NO_PASS=2;

    /**
     * 满标复审通过
     */
    public static final Integer PASS=3;

    /**
     * 复审不通过
     */
    public static final Integer RECHECK_NO_PASS=4;

    /**
     * 已取消；
     */
    public static final Integer CANCEL=5;


}
