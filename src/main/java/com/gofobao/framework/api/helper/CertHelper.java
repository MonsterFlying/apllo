package com.gofobao.framework.api.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 证书帮助类
 * Created by Max on 17/5/18.
 */
@Component
@Slf4j
public class CertHelper {
    private static final String ENCODING = "UTF-8";

    private static final String ALGORITHM = "SHA1withRSA";// 加密算法  or "MD5withRSA"
    @Value("${jixin.keysPath}")
    String keyPath;

    @Value("${jixin.keysPassword}")
    String keyPassword;

    @Value("${jixin.crtPath}")
    String certPath;

    /**
     * 公约
     */
    private PublicKey publicKey;
    /**
     * 私钥
     */
    private PrivateKey privateKey;

    /**
     * 验证公钥
     */
    private PublicKey verifyPublicKey ;

    @PostConstruct
    public void init() {
        // 初始化公私钥
        checkNotNull(keyPath, "###即信公钥路径未配置");
        checkNotNull(keyPassword, "###即信公钥密码未配置");
        checkNotNull(certPath, "###即信服务器证书为空");

        File keyFile = new File(keyPath);
        checkArgument(keyFile.exists(), "即信公钥路劲中的文件不存在");
        KeyStore keyStore = null;
        char[] pwds = keyPassword.toCharArray();
        try (InputStream is = new FileInputStream(keyFile)) {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, pwds);
        } catch (Throwable e) {
            log.error("加载即信公私钥异常", e);
            return;
        }


        try {
            Enumeration<String> aliases = keyStore.aliases();
            String keyAliases = null;

            while (aliases.hasMoreElements()) {
                keyAliases = aliases.nextElement();
                if (keyStore.isKeyEntry(keyAliases)) {
                    break;
                }
            }

            if (StringUtils.isEmpty(keyAliases)) {
                log.error("即信证书alias 不存在");
                return;
            }

            this.privateKey = (PrivateKey) keyStore.getKey(keyAliases, pwds);
            this.publicKey = keyStore.getCertificate(keyAliases).getPublicKey();
            log.info("即信公私钥初始成功");
        } catch (Throwable e) {
            log.error("初始化即信公钥私钥异常", e);
            throw new RuntimeException(e);
        }

        File certFile = new File(certPath) ;
        checkArgument(certFile.exists(), "即信验证参数公钥路劲中的文件不存在");
        try (InputStream is = new FileInputStream(certFile)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate certificate = factory.generateCertificate(is);
            verifyPublicKey = certificate.getPublicKey() ;
            log.info("即信验证参数公私钥初始成功");
        } catch (Throwable e) {
            log.error("即信验证参数公钥异常", e);
            return;
        }
    }

    /**
     * 签名
     *
     * @param unSign 待加签字符串
     * @return 加签字符串
     */
    public String doSign(String unSign) {
        checkNotNull(unSign, "待加密字符串为空");
        log.info(String.format("待签名字符串: %s", unSign));
        String sign = null ;
        try {
            byte[] dataBytes = unSign.getBytes(ENCODING);
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            signature.update(dataBytes);
            byte[] signData = signature.sign();
            sign = Base64Utils.encodeToString(signData);
            log.info(String.format("以签名字符串: %s", sign));
        } catch (Throwable e) {
            log.error("即信加密异常", e);
            return null;
        }

        return sign ;
    }


    /**
     * 验签
     * @param unSignData 待验签字符串
     * @param sign 签名
     * @return true 通过， false 未通过
     */
    public boolean verify(String unSignData, String sign) {
        checkNotNull(unSignData, "待验签字符串为空") ;
        checkNotNull(sign, "签名为空") ;
        try {
            byte[] signBytes = Base64Utils.decodeFromString(sign);
            byte[] unSignBytes = unSignData.getBytes(ENCODING);
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(verifyPublicKey);
            signature.update(unSignBytes);
            return signature.verify(signBytes);
        } catch (Throwable e) {
            log.error("即信验签异常", e);
            return false;
        }
    }

}



