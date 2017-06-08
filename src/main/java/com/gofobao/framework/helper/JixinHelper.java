package com.gofobao.framework.helper;

import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * Created by Zeke on 2017/6/1.
 */
public class JixinHelper {
    /**
     * 今天第一产生批次号时间
     */
    private static Date firstCreateAt;
    /**
     * 批次编号 100000开始
     */
    private static int no;

    public static final String TENDER_PREFIX = "GFBT_";
    public static final String LEND_PAY_PREFIX = "GFBLP_";

    public static String getOrderId(String prefix) {
        return prefix + new Date().getTime();
    }

    public static String getBatchNo() {
        Date nowDate = new Date();
        if (ObjectUtils.isEmpty(firstCreateAt) || DateHelper.beginOfDate(nowDate).getTime() > firstCreateAt.getTime()) {
            no = 100000;
            firstCreateAt = nowDate;
        }
        if (no<1000000){
            return StringHelper.toString(no++);
        }
        return null;
    }
}
