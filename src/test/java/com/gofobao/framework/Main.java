package com.gofobao.framework;

import com.gofobao.framework.helper.ReleaseHelper;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        publishRedpack() ;
    }


    /**
     * 重新触发派发红包方法
     * @return
     */
    private static boolean publishRedpack() {
        String url = "https://api.gofobao.com/pub/publishActivity/red";
        //String url = "http://127.0.0.1:8080/pub/admin/companyAccountInfo" ;
        Map<String, String> data = new HashMap<>();
        data.put("beginTime", "2017-10-21 17:44:00");
        ReleaseHelper.sendMsg(url, data);
        return true;
    }
}
