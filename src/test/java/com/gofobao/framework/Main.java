package com.gofobao.framework;

import com.gofobao.framework.helper.ReleaseHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        publishRedpack();
    }


    /**
     * 重新触发派发红包方法
     *
     * @return
     */
    private static boolean publishRedpack() {
        String url = "https://api.gofobao.com/pub/publishActivity/red";
        Map<String, String> data = new HashMap<>();
        data.put("beginTime", "2017-11-09 00:00:00");
        ReleaseHelper.sendMsgByPost(url, data);
        return true;
    }


    /**
     * 离线拨正充值记录
     *
     * @return
     */
    private static boolean statementRechargeOffline() {
        String url = "https://api.gofobao.com/pub/rechargeStatement/offline";
        Map<String, String> data = new HashMap<>();
        data.put("date", "2017-09-30 11:37:50");
        data.put("id", "5930");
        ReleaseHelper.sendMsgByPost(url, data);
        return true;
    }


    private static boolean changeRecord() {
        String url = "https://api.gofobao.com/pub/rechargeStatement/offline";
        Map<String, String> data = new HashMap<>();
        data.put("date", "2017-09-30 11:37:50");
        data.put("id", "5930");
        ReleaseHelper.sendMsgByPost(url, data);
        return true;
    }


    public static boolean checkUpAccountAll() {
        String url = "https://api.gofobao.com/pub/asset/check-up-all-account";
        // String url = "http://127.0.0.1:8080/pub/asset/check-up-all-account";
        Map<String, String> data = new HashMap<>();
        data.put("id", "59310");
        ReleaseHelper.sendMsgByPost(url, data);
        return true ;
    }

    public static boolean checkUpAccountPartial() {
        String url = "https://api.gofobao.com/pub/asset/check-up-partial-account";
        Map<String, String> data = new HashMap<>();
        data.put("id", "5930");
        ReleaseHelper.sendMsgByPost(url, data);
        return true ;
    }

    public static boolean checkUpAccountActive() {
       String url = "https://api.gofobao.com/pub/asset/check-up-active-account";
        //String url = "http://127.0.0.1:8080/pub/asset/check-up-active-account";
        Map<String, String> data = new HashMap<>();
        data.put("id", "5930");
        ReleaseHelper.sendMsgByPost(url, data);
        return true ;
    }
}
