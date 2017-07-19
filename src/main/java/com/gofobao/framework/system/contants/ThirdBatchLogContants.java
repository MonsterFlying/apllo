package com.gofobao.framework.system.contants;

/**
 * Created by Zeke on 2017/6/15.
 */
public class ThirdBatchLogContants {
    //存管批次日志类型
    public static final int BATCH_CREDIT_INVEST = 1; /* 1.投资人批次购买债权 */
    public static final int BATCH_LEND_REPAY = 2; /* 2.即信批次放款 */
    public static final int BATCH_REPAY = 3; /* 3.即信批次还款 */
    public static final int BAIL_REPAY = 4; /* 4.担保人垫付 */
    public static final int REPAY_BAIL = 5;/*批次融资人还担保账户垫款*/

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
}
