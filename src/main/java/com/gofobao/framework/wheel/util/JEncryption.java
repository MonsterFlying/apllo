package com.gofobao.framework.wheel.util;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
public class JEncryption {

    public static String encrypt(byte[] data, String key) throws Exception {
        SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sf.generateSecret(new DESKeySpec(key.getBytes("UTF-8"))));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data));
    }

    public static String decrypt(String data, String key) throws Exception {
        SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sf.generateSecret(new DESKeySpec(key.getBytes("UTF-8"))));
        byte[] bytes = Base64.getDecoder().decode(data);
        return new String(cipher.doFinal(bytes), "UTF-8");
    }
    public static void main(String[] argv) {
        try {
            String key = "dsDaNZY7";
            String enc = encrypt("AAA".getBytes("UTF-8"), key);
            System.out.println(enc);

            String v = decrypt(enc, key);
            System.out.println(v);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
