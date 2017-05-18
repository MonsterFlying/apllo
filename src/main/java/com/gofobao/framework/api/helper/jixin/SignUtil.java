package com.gofobao.framework.api.helper.jixin;

import com.gofobao.framework.api.request.AbsRequest;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.javafx.collections.MappingChange;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Created by Zeke on 2017/5/17.
 */
public class SignUtil {
    private static Gson gson = new GsonBuilder().create();
    private static String keys = "D:/sign/yb_sit.p12";//私钥
    private static String pass = "yibao_sit";//私钥密码
    private static String crt = "D:/sign/fdep.crt";//服务端证书

    /**
     * 请求参数签名
     * @param absRequest
     * @return
     */
    public static String sign(AbsRequest absRequest) {
        String sign = null;
        RSAHelper signer = null;
        try {
            RSAKeyUtil e = new RSAKeyUtil(new File(keys), pass);
            signer = new RSAHelper(e.getPrivateKey());
            Map<String, String> tempMap = gson.fromJson(gson.toJson(absRequest), new TypeToken<Map<String, String>>() {
            }.getType());

            sign = signer.sign(getSignParamMap(tempMap));

        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return sign;
    }

    /**
     * 验证签名
     *
     * @param bodyJson
     * @return
     */
    public static boolean verify(String bodyJson) {
        boolean bool = false;
        try {
            Map<String, String> bodyMap = gson.fromJson(bodyJson, new TypeToken<Map<String, String>>() {
            }.getType());
            String signStr = bodyMap.get("sign");
            String respStr = getSignParamMap(bodyMap);
            RSAKeyUtil e = new RSAKeyUtil(new File(crt));
            RSAHelper signHelper = new RSAHelper(e.getPublicKey());
            bool = signHelper.verify(respStr, signStr);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        return bool;
    }

    /**
     * 获取请求参数集合签名
     *
     * @param map
     * @return
     */
    public static String getSignParamMap(Map<String, String> map) {

        TreeMap reqMap = new TreeMap(map);
        StringBuffer buff = new StringBuffer();
        Iterator iter = reqMap.entrySet().iterator();

        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            if (!"sign".equals(entry.getKey())) {
                if (entry.getValue() == null) {
                    entry.setValue("");
                    buff.append("");
                } else {
                    buff.append(String.valueOf(entry.getValue()));
                }
            }
        }

        String requestMerged = buff.toString();
        return requestMerged;
    }
}
