package com.gofobao.framework.api.helper;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import com.gofobao.framework.api.request.JixinBaseRequest;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Max on 17/5/19.
 */
@Component
@Slf4j
public class JixinManager {
    @Autowired
    CertHelper certHelper;

    Gson gson = new Gson();

    @Value("${jixin.url}")
    String prefixUrl;

    @Value("${jixin.version}")
    String version;

    @Value("${jixin.instCode}")
    String instCode;

    @Value("${jixin.bankCode}")
    String bankCode;

    public <T extends JixinBaseRequest> String getHtml(JixinTxCodeEnum txCodeEnum, T req) {
        checkNotNull(req, "请求体为null");
        // 前期初始化
        req.setBankCode(bankCode);
        req.setVersion(version);
        req.setInstCode(instCode);

        if (StringUtils.isEmpty(req.getSeqNo())) {
            req.setSeqNo(RandomHelper.generateNumberCode(6));
        }

        if (StringUtils.isEmpty(req.getTxTime())) {
            req.setTxTime(DateHelper.getTime());
        }

        if (StringUtils.isEmpty(req.getTxDate())) {
            req.setTxDate(DateHelper.getDate());
        }

        req.setTxCode(txCodeEnum.getValue());
        String url = prefixUrl + txCodeEnum.getUrl();
        String json = gson.toJson(req);
        Map<String, String> params = gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
        String unSign = StringHelper.mergeMap(params);
        String sign = certHelper.doSign(unSign);
        params.put("sign", sign);
        log.info(String.format("即信请求报文: url=%s body=%s", url, gson.toJson(params)));
        return genFormHtml(params, url);
    }

    /**
     * 创建form表单
     *
     * @param params 参数
     * @param url    链接
     * @return
     */
    private String genFormHtml(Map<String, String> params, String url) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("     <title>广富宝金服-江西银行存管系统</title>");
        sb.append("     <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /> ");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<form method=\"post\" action=\"").append(url).append("\">");
        if (!CollectionUtils.isEmpty(params)) {
            Set<Map.Entry<String, String>> entries = params.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                sb.append("    <input type=\"hidden\" name=\"").append(next.getKey()).append("\" value=\"").append(next.getValue()).append("\"/> ");
            }

        }
        sb.append("</form>");
        sb.append("<script>\n" +
                "    document.getElementsByTagName('form')[0].submit();\n" +
                "</script>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    private HttpEntity getHttpEntity(Map<String, String> params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Accept-Charset", "UTF-8");
        httpHeaders.set("contentType", "application/json");
        return new HttpEntity(params, httpHeaders);
    }

    public <T extends JixinBaseResponse> T callback(HttpServletRequest request, TypeToken<T> typeToken) {
        checkNotNull(request, "请求体为null");
        String bgData = request.getParameter("bgData");
        log.info(String.format("即信异步响应返回值：%s", bgData));
        checkNotNull(bgData, "返回值内容为空");
        T t = gson.fromJson(bgData, typeToken.getType());
        // 验证参数
        Map<String, String> param = gson.fromJson(bgData, new TypeToken<Map<String, String>>() {
        }.getType());
        String unsige = StringHelper.mergeMap(param);
        boolean result = certHelper.verify(unsige, param.get("sign"));
        if (!result) {
            log.error("验签失败", bgData);
            return null;
        }

        return t;
    }

    public <T extends JixinBaseRequest, S extends JixinBaseResponse> S send(JixinTxCodeEnum txCodeEnum, T req, Class<S> clazz) {
        checkNotNull(req, "请求体为null");
        // 前期初始化
        req.setBankCode(bankCode);
        req.setVersion(version);
        req.setInstCode(instCode);

        if (StringUtils.isEmpty(req.getSeqNo())) {
            req.setSeqNo(RandomHelper.generateNumberCode(6));
        }

        if (StringUtils.isEmpty(req.getTxTime())) {
            req.setTxTime(DateHelper.getTime());
        }

        if (StringUtils.isEmpty(req.getTxDate())) {
            req.setTxDate(DateHelper.getDate());
        }

        req.setTxCode(txCodeEnum.getValue());
        String url = prefixUrl + txCodeEnum.getUrl();
        String json = gson.toJson(req);
        Map<String, String> params = gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
        String unSign = StringHelper.mergeMap(params);
        String sign = certHelper.doSign(unSign);
        params.put("sign", sign);
        log.info(String.format("即信请求报文: url=%s body=%s", url, gson.toJson(params)));
        initHttps();
        HttpEntity entity = getHttpEntity(params);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<S> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, clazz);
        } catch (Throwable e) {
            log.error("请求即信服务器异常", e);
            return null;
        }

        checkNotNull(response);
        S body = response.getBody();
        // 验证参数
        String bodyJson = gson.toJson(body);
        Map<String, String> unverifyParams = gson.fromJson(bodyJson, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String unsige = StringHelper.mergeMap(unverifyParams);
        boolean result = certHelper.verify(unsige, unverifyParams.get("sign"));
        if (!result) {
            log.error("即信请求返回值验证不通过");
            return null;
        }
        log.info(String.format("即信响应报文:url=%s body=%s", url, gson.toJson(body)));

        // 请求插入数据
        return body;
    }

    private void initHttps() {
        try {
            TrustManager[] trustManagers = new TrustManager[1];
            TrustManager tm = new SimpleTrustManager();
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
        } catch (Exception ex) {
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