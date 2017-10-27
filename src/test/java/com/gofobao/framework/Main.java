package com.gofobao.framework;

import com.gofobao.framework.helper.ReleaseHelper;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        statementRechargeOffline() ;
    }


    /**
     * 重新触发派发红包方法
     * @return
     */
    private static boolean publishRedpack() {
        String url = "https://api.gofobao.com/pub/publishActivity/red";
        Map<String, String> data = new HashMap<>();
        data.put("beginTime", "2017-10-21 17:44:00");
        ReleaseHelper.sendMsg(url, data);
        return true;
    }


    /**
     * 离线拨正充值记录
     * @return
     */
    private static boolean statementRechargeOffline(){
        String url = "https://api.gofobao.com/pub/rechargeStatement/offline";
        Map<String, String> data = new HashMap<>();
        data.put("date", "2017-09-30 11:37:50");
        data.put("id", "5930") ;
        ReleaseHelper.sendMsg(url, data);
        return true;
    }
}
