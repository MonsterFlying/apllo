package com.gofobao.framework.helper;

import java.util.Date;

/**
 * Created by Zeke on 2017/6/1.
 */
public class JixinHelper {
    public static final String TENDER_PREFIX = "GFBT_";

    public static String getTenderOrderId() {
        return TENDER_PREFIX + new Date().getTime();
    }
}
