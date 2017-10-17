package com.gofobao.framework;

import com.gofobao.framework.helper.ReleaseHelper;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String url = "https://api.gofobao.com/pub/admin/companyAccountInfo";
        //String url = "http://127.0.0.1:8080/pub/admin/companyAccountInfo" ;
        Map<String, String> data = new HashMap<>();
        data.put("id", "6212462190000000021");
        ReleaseHelper.sendMsg(url, data);
    }
}
