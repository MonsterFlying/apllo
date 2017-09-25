package com.gofobao.framework.system.contants;

/**
 * Created by Zeke on 2017/6/15.
 */
public class ThirdBatchLogContants {
    //存管批次日志类型
    public static final int BATCH_CREDIT_INVEST = 1; /* 1.投资人批次购买债权 */
    public static final int BATCH_LEND_REPAY = 2; /* 2.批次即信批次放款 */
    public static final int BATCH_REPAY = 3; /* 3.批次即信批次还款 */
    public static final int BATCH_BAIL_REPAY = 4; /* 4.批次名义借款人垫付 */
    /*public static final int BATCH_REPAY_BAIL = 5;批次融资人还担保账户垫款*/
    public static final int BATCH_CREDIT_END = 6;/*批次结束投资人债权*/
    public static final int BATCH_REPAY_ALL = 7;/*提前结清批次还款*/
    public static final int BATCH_FINANCE_CREDIT_INVEST = 8; /*理财计划批次购买债权 */

    //存管批次状态
    public static final String AWAIT_DISPOSE = "A";/* 等待处理 */
    public static final String DISPOSING = "D";/* 处理中 */
    public static final String PROCESSED = "S"; /* 处理成功 */
    public static final String FAILURE_DISPOSE = "F"; /* 处理失败 */
    public static final String DESTROY_DISPOSE = "C"; /* 已撤销 */

    //checkBatchOftenSubmit 返回状态
    public static final int SUCCESS = 0; //批次成功处理
    public static final int VACANCY = -1; //暂未发布发送批次
    public static final int AWAIT = 1; //批次处理中

    //存管报备状态
    public static final String BORROW_FIRST_STEP = "标的存管报备";
    public static final String BORROW_SECOND_STEP = "标的满标时间";
    public static final String BORROW_THIRD_STEP = "存管系统接受平台放款请求";
    public static final String BORROW_FOUR_STEP = "存管确定平台放款请求";
    public static final String BORROW_FIVE_STEP = "存管放款";

    //存管报备状态
    public static final String REPAYMENT_FIRST_STEP = "借款本地还款";
    public static final String REPAYMENT_SECOND_STEP = "存管系统接受平台还款请求";
    public static final String REPAYMENT_THIRD_STEP = "存管确定平台还款请求";
    public static final String REPAYMENT_FOUR_STEP = "存管还款";
}
