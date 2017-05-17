package com.gofobao.framework.api.helper.jixin;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.util.Base64Utils;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;

public class RSAKeyUtil {
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	public RSAKeyUtil(File certFile) throws FileNotFoundException {
		InputStream is = new FileInputStream(certFile);
		this.publicKey=getPublicKey(is);
	}
	
	public RSAKeyUtil(File keysFile,String pwd) throws GeneralSecurityException, IOException {
		InputStream is = new FileInputStream(keysFile);
		char[] pwds = pwd.toCharArray();
		KeyStore ks = getKeyStore(is,pwds);
		String alias = getKeyAlias(ks);
		if(alias!=null){
			this.privateKey=(PrivateKey) ks.getKey(alias, pwds);
			this.publicKey=ks.getCertificate(alias).getPublicKey();
		}
	}

	/**
	 * @param certStream pem  Stream
	 * @return
	 */
	public static PublicKey getPublicKey(InputStream certStream) {
		try {
			// 开始获取公钥
			if (certStream != null) {
				// 通过加密算法获取公钥
				Certificate cert = null;
				try {
					CertificateFactory cf = CertificateFactory.getInstance("X.509"); // 指定证书类型
					cert = cf.generateCertificate(certStream); // 获取证书
					return cert.getPublicKey(); // 获得公钥
				} finally {
					if (certStream != null) {
						certStream.close();
					}
				}
			}
		} catch (IOException e) {
			System.out.println("无法获取url连接");
			e.printStackTrace();
		} catch (CertificateException e) {
			System.out.println("获取证书失败");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param keyBytes
	 * @return
	 */
	public static PublicKey getPublicKey(byte[] keyBytes) {
		try {			
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("初始化加密算法时报错");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			System.out.println("初始化公钥时报错");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param keyBytes PKCS8Encoded key bytes
	 * @return
	 */
	public static PrivateKey getPrivateKey(byte[] keyBytes) {
		try {			
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			//System.out.println("初始化加密算法时报错");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			//System.out.println("初始化私钥时报错");
			e.printStackTrace();
		}
		return null;
	}
	
	private static KeyStore getKeyStore(InputStream is, char[] pwds) throws IOException, GeneralSecurityException{
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(is,  pwds);
		is.close();
		return ks;
	}
	
	private static String getKeyAlias(KeyStore ks) throws KeyStoreException {
		Enumeration<String> enuml = ks.aliases();
		String keyAlias = null;
		if (enuml.hasMoreElements()) {
			keyAlias = enuml.nextElement();
			if(ks.isKeyEntry(keyAlias)){
				return keyAlias;
			}
		}
		return null;
	}

	public static void testGenerateKeyPair() {
		try {
			KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
			keygen.initialize(1024,  new SecureRandom("credit2go".getBytes()));
			KeyPair keys = keygen.genKeyPair();
			PublicKey publicKey = keys.getPublic();
			PrivateKey privateKey = keys.getPrivate();

			System.out.println("publicKey : " + new String(Hex.encode(publicKey.getEncoded())));
			System.out.println("publicKey : " + Base64Utils.encodeToString(publicKey.getEncoded()));

			System.out.println("privateKey: " + new String(Hex.encode(privateKey.getEncoded())));
			System.out.println("privateKey: " + Base64Utils.encodeToString(privateKey.getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
