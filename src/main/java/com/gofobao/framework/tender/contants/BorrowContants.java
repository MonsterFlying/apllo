package com.gofobao.framework.tender.contants;

/**
 * 标类型
 * Created by Max on 17/3/8.
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
     * 秒标
     */
    public static final int MIAO = 2;
    /**
     * 渠道标
     */
    public static final int QU_DAO = 4;
    /**
     * 全部
     */
    public static final int INDEX_TYPE_ALL = 0;
    /**
     * 车贷标
     */
    public static final int INDEX_TYPE_CE_DAI = 1;
    /**
     * 净值标
     */
    public static final int INDEX_TYPE_JING_ZHI = 2;
    /**
     * 秒标
     */
    public static final int INDEX_TYPE_MIAO = 3;
    /**
     * 渠道标
     */
    public static final int INDEX_TYPE_QU_DAO = 4;
    /**
     * 转让标
     */
    public static final int INDEX_TYPE_LIU_ZHUAN = 5;

    /**
     * 获取用户借款列表 获取用户借款列表
     */
    public static final int REPAYMENT = 1;//还款中
    public static final int CALL_FOR_BORROW = 2;//招标中
    public static final int COMPLETE = 3;//已完成

    /**
     *
     */
    public static final String DAY = "天";


    public static final String MONTH = "个月";


    public static final String REPAY_FASHION_AYFQ = "0";//按月分期
    public static final String REPAY_FASHION_YCBX = "1";//一次性还本付息
    public static final String REPAY_FASHION_XXHB = "2";//先息后本
    public static final int REPAY_FASHION_AYFQ_NUM = 0;//按月分期
    public static final int REPAY_FASHION_YCBX_NUM = 1;//一次性还本付息
    public static final int REPAY_FASHION_XXHB_NUM = 2;//先息后本



    public static final Integer PENDING=0; //发标待审
    public static final Integer NO_PASS=0; //初审不通过
    public static final Integer PASS=0; //满标复审通过
    public static final Integer RECHECK_NO_PASS=0; //复审不通过
    public static final Integer CANCELED=0; //已取消






}
