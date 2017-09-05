package com.gofobao.framework.api.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.ObjectUtils;
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

    @Value(value = "classpath:certs/guangfubao_uat.p12")
    Resource p12Resource;  // p12 文件

    @Value("${jixin.keysPassword}")
    String keyPassword;

    @Value(value = "classpath:certs/fdep.crt")
    Resource crtResource;  // 即信公钥文件

    @Value("${jixin.crt-path}")
    private String crtPath;

    @Value("${jixin.p12-path}")
    private String p12Path;


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
    private PublicKey verifyPublicKey;

    @PostConstruct
    public void init() {
        // 初始化公私钥

        checkNotNull(keyPassword, "###即信公钥密码未配置");
        if (StringUtils.isEmpty(crtPath)) {
            checkNotNull(crtResource, "###即信服务器证书文件为空");
        } else {
            File file = new File(crtPath);
            if (ObjectUtils.isEmpty(file)) {
                log.error(String.format("加载公钥失败路径: %s", crtPath));
                throw new RuntimeException("即信公钥加载失败");
            }
        }

        if (StringUtils.isEmpty(p12Path)) {
            checkNotNull(p12Resource, "###即信公钥文件不存在");
        } else {
            File file = new File(p12Path);
            if (ObjectUtils.isEmpty(file)) {
                log.error(String.format("加载私钥失败路径: %s", p12Path));
                throw new RuntimeException("即信私钥加载失败");
            }
        }

        KeyStore keyStore = null;
        char[] pwds = keyPassword.toCharArray();
        if (StringUtils.isEmpty(p12Path)) {
            try (InputStream is = p12Resource.getInputStream()) {
                keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(is, pwds);
            } catch (Throwable e) {
                log.error("加载即信公私钥异常", e);
                return;
            }
        } else {
            try (InputStream is = new FileInputStream(p12Path)) {
                keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(is, pwds);
            } catch (Throwable e) {
                log.error("加载即信公私钥异常", e);
                return;
            }
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

        if (StringUtils.isEmpty(crtPath)) {
            try (InputStream is = crtResource.getInputStream()) {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                Certificate certificate = factory.generateCertificate(is);
                verifyPublicKey = certificate.getPublicKey();
                log.info("即信验证参数公私钥初始成功");
            } catch (Throwable e) {
                log.error("即信验证参数公钥异常", e);
                return;
            }
        } else {
            try (InputStream is = new FileInputStream(crtPath)) {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                Certificate certificate = factory.generateCertificate(is);
                verifyPublicKey = certificate.getPublicKey();
                log.info("即信验证参数公私钥初始成功根据外部文件");
            } catch (Throwable e) {
                log.error("即信验证参数公钥异常", e);
                return;
            }
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
        String sign = null;
        try {
            byte[] dataBytes = unSign.getBytes(ENCODING);
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            signature.update(dataBytes);
            byte[] signData = signature.sign();
            sign = Base64Utils.encodeToString(signData);
        } catch (Throwable e) {
            log.error("即信加密异常", e);
            return null;
        }

        return sign;
    }


    /**
     * 验签
     *
     * @param unSignData 待验签字符串
     * @param sign       签名
     * @return true 通过， false 未通过
     */
    public boolean verify(String unSignData, String sign) {
        checkNotNull(unSignData, "待验签字符串为空");
        checkNotNull(sign, "签名为空");
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



