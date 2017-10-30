package com.gofobao.framework.wheel.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.util.Base64;

public class JEncryption {

	/**
	 * 加密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(byte[] data, String key) throws Exception {
		SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, sf.generateSecret(new DESKeySpec(key.getBytes("UTF-8"))));
		return Base64.getEncoder().encodeToString(cipher.doFinal(data));
	}

	/**
	 * 解密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String data, String key) throws Exception {
		SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, sf.generateSecret(new DESKeySpec(key.getBytes("UTF-8"))));
		byte[] bytes = Base64.getDecoder().decode(data);
		return new String(cipher.doFinal(bytes), "UTF-8");
	}

	public static void main(String[] argv) {
		try {
			String key = "f30a7dfd";
			String enc = encrypt("AAA".getBytes("UTF-8"), key);
			System.out.println(enc);

			String v = decrypt(enc, key);
			System.out.println(v);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
