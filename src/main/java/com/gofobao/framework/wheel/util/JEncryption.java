package com.gofobao.framework.wheel.util;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.util.Base64;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
public class JEncryption {

   private static String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";

    public static String encrypt(byte[] data, String key) throws Exception {
        SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, sf.generateSecret(new DESKeySpec(key.getBytes("UTF-8"))));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data));
    }

    public static String decrypt(String data, String key) throws Exception {
        SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sf.generateSecret(new DESKeySpec(key.getBytes("UTF-8"))));
        byte[] bytes = Base64.getDecoder().decode(data);
        return new String(cipher.doFinal(bytes), "UTF-8");
    }
    public static void main(String[] argv) {
        try {
           String key = "dsDaNZY7";
             String enc = encrypt("invest_id=183072&invest_title=车轮用户投资通知测试&buy_unit=50.00&buy_limit=1000.00&invest_url=/#/borrow/183072&time_limit=30&time_limit_desc=1个月&total_amount=100000.00&rate=10.00&progress=14%&payback_way=等额本息&invest_condition=&project_description=<p>车轮用户投资通知测试</p>&lose_invest=0".getBytes("UTF-8"), key);

            Map<String, Object> paramMap = Maps.newHashMap();
            paramMap.put("param",enc);
            System.out.print(new Gson().toJson(enc));


             System.out.println("加密字段："+enc);
            String encStr="cG9fXWTwwQ32T5qo18Vs4HoDxpqh7jWPfC7PNiRdHOSIrcgbmdH1iwgpx17C4fcrUlCm84za3RefQlNuxQDwYJQqcHyi3c80UjpozUuaDHvhr/R0Z3gytU/AxjDDxUPSd78U5XtdR95Qnnm/ZahpqIORwd+ztKPFJf2hFks768xFlm7dS/Pjghu+pJDvekwY7Ut+ZXFp1CegwY73xZ/95/OklpeB1X6S1VnEgPDxFNzQwrAeWvaIi4mbMKiXgkj9B6ubxqBP74DLA8f4VWMguxwB26rISstohEZZae2+s3gGw1CRFbvUy8L66+ucCsOQJx66r/VXvNMs3dGDpyx6Y/T2zdEWNo/NXR67sY8/Dwl87ToDXG9MyRmJZ8uBdcEU7SuIHrvNziYx8GpzjHeVHMzq9dH6dPrRkg6G6xfnNF7Tm3X2qSTsRg\u003d\u003d";
            String decryptStr = decrypt(encStr, key);
            System.out.println("解密字段："+decryptStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
