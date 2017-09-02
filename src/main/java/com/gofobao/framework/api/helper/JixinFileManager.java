package com.gofobao.framework.api.helper;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 即信文件下载帮助类
 */
@Component
@Slf4j
public class JixinFileManager {
    @Value("${jixin.file-url}")
    String fileUrl ;

    @Value("${jixin.save-file-path}")
    String saveFileUrl ;

    @Value("${jixin.version}")
    String version;

    @Value("${jixin.instCode}")
    String instCode;

    @Value("${jixin.bankCode}")
    String bankCode;

    @Autowired
    CertHelper certHelper;

    @Autowired
    JixinHelper jixinHelper ;

    public final  static  Gson GSON = new Gson() ;

    public boolean download(String fileName) {
        File newFile = new File(saveFileUrl);  //文件在本地的存放地址
        if (!newFile.exists() && !newFile.isDirectory()) {
            newFile.mkdirs();
        }
        String saveFile = String.format("%s%s%s", saveFileUrl, File.separator, fileName) ;
        File file = new File(saveFile);
        if(file.exists()){
            return true ;
        }
        Map<String, String> params = new HashMap<>();
        params.put("instCode", instCode);
        params.put("bankCode", bankCode);
        params.put("fileName", fileName);
        params.put("txDate", fileName.substring(fileName.length() - 8));
        String unSign = StringHelper.mergeMap(params);
        String sign = certHelper.doSign(unSign);
        params.put("SIGN", sign);
        initHttps();
        HttpEntity entity = getHttpEntity(params);
        RestTemplate restTemplate = new RestTemplate(new ArrayList<HttpMessageConverter<?>>() {{
            add(new GsonHttpMessageConverter());
        }});
        ResponseEntity<Map> response = null;
        try {
            response = restTemplate.exchange(fileUrl, HttpMethod.POST, entity, Map.class);
        } catch (Throwable e) {
            log.error("文件下载: 请求即信服务器异常", e);
            return false;
        }
        checkNotNull(response);
        // 验证参数
        String bodyJson = GSON.toJson(response.getBody());
        Map<String, String> responseMap = GSON.fromJson(bodyJson, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Map<String, String> mapOfRemoveFile = new HashMap<>(responseMap);
        mapOfRemoveFile.remove("FILE");
        String unsige = StringHelper.mergeMap(mapOfRemoveFile);
        boolean result = certHelper.verify(unsige, mapOfRemoveFile.get("SIGN"));
        if (!result) {
            log.error("文件下载: 参数验证失败!");
            return false;
        }
        String returnCode = responseMap.get("returnCode");
        if (!"0000".equals(returnCode)) {
            log.error("下载文件: 响应代码错误");
            return false;
        }


        String fileContent = responseMap.get("FILE");
        String md5 = responseMap.get("MD5");
        if (!md5.equalsIgnoreCase(DigestUtils.md5Hex(fileContent))) {
            log.error("下载文件:文件MD5验证错误");
            return false;
        }

        FileOutputStream out = null;
        try {
            byte[] fileData = fileContent.getBytes("UTF-8");
            out = new FileOutputStream(file);
            out.write(fileData);
        } catch (Exception e) {
            log.error("即信请求文件, 下载为空!", e);
            return false ;
        } finally {
            if (!ObjectUtils.isEmpty(out)) {
                try {
                    out.close();
                } catch (IOException e) {
                    out = null;
                }
            }
        }
        log.info("下载文件: 成功");
        return true ;
    }


    private HttpEntity getHttpEntity(Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json; charset=utf-8"));
        headers.set("Accept-Charset", "UTF-8");
        return new HttpEntity( params, headers);
    }

    private void initHttps() {
        try {
            TrustManager[] trustManagers = new TrustManager[1];
            TrustManager tm = new JixinManager.SimpleTrustManager();
            trustManagers[0] = tm;
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Throwable ex) {
            log.error("即信通讯工具：初始化https失败", ex);
        }
    }

    public static class SimpleTrustManager implements TrustManager, X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
