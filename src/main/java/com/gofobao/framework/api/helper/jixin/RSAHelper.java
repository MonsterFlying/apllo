package com.gofobao.framework.api.helper.jixin;

import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;

public class RSAHelper {
	private String encoding = "UTF-8";
	private String algorithm = "SHA1withRSA";// 加密算法  or "MD5withRSA"
	private PublicKey publicKey ;
	private PrivateKey privateKey ;

	public RSAHelper(PublicKey publicKey) {
		this.publicKey=publicKey;
	}
	
	public RSAHelper(PrivateKey privateKey) {
		this.privateKey=privateKey;
	}

	/**
	 * 
	 * Description:校验数字签名,此方法不会抛出任务异常,成功返回true,失败返回false,要求全部参数不能为空
	 *
	 * @param dataText
	 *            明文
	 * @param signText
	 *            数字签名的密文,base64编码
	 * @return 校验成功返回true 失败返回false
	 */
	public boolean verify(String dataText, String signText) {
		try {
			byte[] signBytes = Base64Utils.decodeFromString(signText);
			byte[] dataBytes = dataText.getBytes(encoding);
			return verify(dataBytes, signBytes);
		} catch (UnsupportedEncodingException e) {
			System.out.println("编码错误");
			e.printStackTrace();
		}
		// 取公钥匙对象
		return false;
	}

	/**
	 * 验签 signature:加密串参数signature srcData：参数值的拼接串
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public boolean verify(byte[] srcData, byte[] signData) {
		try {
			Signature sig = Signature.getInstance(algorithm); 
			sig.initVerify(publicKey); // 初始化公钥用于验证的对象
			sig.update(srcData); // 验证数据
			return sig.verify(signData); // 验证传入的签名
		} catch (NoSuchAlgorithmException e) {
			System.out.println("初始化加密算法时报错");
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			System.out.println("初始化公钥时报错");
			e.printStackTrace();
		} catch (SignatureException e) {
			System.out.println("验证数据时报错");
			e.printStackTrace();
		} catch (Throwable e) {
			System.out.println("校验签名失败");
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 签名
	 * @param dataText 数据
	 * @return
	 */
	public String sign(String dataText){
		try {
			byte[] dataBytes = dataText.getBytes(encoding);
			byte[] signed = sign(dataBytes); 
			return Base64Utils.encodeToString(signed);
		} catch (UnsupportedEncodingException e) {
			System.out.println("初始化加密算法时报错");
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * @param srcData 数据
	 * @return
	 */
	public byte[] sign(byte[] srcData) {
		try {
			Signature sig = Signature.getInstance(algorithm); 
			sig.initSign(privateKey);
			sig.update(srcData);
			byte[] signed = sig.sign(); // 对信息的数字签名
			return signed; 
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			System.out.println("初始化加密算法时报错");
			e.printStackTrace();
		} catch (Throwable e) {
			System.out.println("校验签名失败");
			e.printStackTrace();
		}
		return null;
	}
	
}
