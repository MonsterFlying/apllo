package com.gofobao.framework.api.helper;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.repsonse.ContractBaseResponse;
import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import com.gofobao.framework.api.request.ContractBaseRequest;
import com.gofobao.framework.api.request.JixinBaseRequest;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Max on 17/5/19.
 */
@Component
@Slf4j
public class ContractManager {
    @Autowired
    CertHelper certHelper;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    private Gson gson;


    @Value("${jixin.contract.crtdata_url}")
    private String crtdataUrl;

    @Value("${jixin.contract.content-url}")
    private String contentUrl;

    @Value("${jixin.contract.ids-url}")
    private String idsUrl;

    @Value("${jixin.contract.crtdata_url}")
    private String htmlPrefixUrl;

    @Value("${jixin.instCode}")
    private String instCode;


    /**
     * 获取url
     *
     * @param txCodeEnum
     * @return
     */
    public String getUrl(JixinTxCodeEnum txCodeEnum) {
        return htmlPrefixUrl + txCodeEnum.getUrl();
    }


    @Data
    public class KeyValuePair {
        private String key;
        private String value;
    }

    /**
     * 获取加签
     *
     * @param txCodeEnum
     * @param req
     * @param <T>
     * @return
     */
    public <T extends JixinBaseRequest> List<KeyValuePair> getSignData(JixinTxCodeEnum txCodeEnum, T req) {
        checkNotNull(req, "JixinManager.getSignData: req is null");
        req.setInstCode(instCode);
        if (StringUtils.isEmpty(req.getSeqNo())) {
            req.setSeqNo(RandomHelper.generateNumberCode(6));
        }

        if (StringUtils.isEmpty(req.getTxTime())) {
            req.setTxTime(DateHelper.getTime());
        }

        if (StringUtils.isEmpty(req.getTxDate())) {
            req.setTxDate(DateHelper.getDateFor24());
        }

        if (StringUtils.isEmpty(req.getChannel())) {
            req.setChannel(ChannelContant.HTML);
        }
        req.setTxCode(txCodeEnum.getValue());
        String json = gson.toJson(req);
        Map<String, String> params = gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        String unSign = StringHelper.mergeMap(params);
        String sign = certHelper.doSign(unSign);
        params.put("sign", sign);

        List<KeyValuePair> datas = new ArrayList<>(params.size());
        Set<String> keys = params.keySet();
        for (String key : keys) {
            String value = params.get(key);
            KeyValuePair keyValuePair = new KeyValuePair();
            keyValuePair.setKey(key);
            keyValuePair.setValue(value);
            datas.add(keyValuePair);
        }

        return datas;
    }


    private HttpEntity getHttpEntity(Map<String, String> params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Accept-Charset", "UTF-8");
        httpHeaders.set("contentType", "application/json");
        return new HttpEntity(params, httpHeaders);
    }

    /**
     * 回调
     *
     * @param request
     * @param typeToken
     * @param <T>
     * @return
     */
    public <T extends JixinBaseResponse> T callback(HttpServletRequest request, TypeToken<T> typeToken) {
        checkNotNull(request, "请求体为null");
        String bgData = request.getParameter("bgData");
        log.info(String.format("即信异步响应返回值：%s", bgData));
        checkNotNull(bgData, "返回值内容为空");
        T t = gson.fromJson(bgData, typeToken.getType());
        // 验证参数
        Map<String, String> param = gson.fromJson(bgData, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String unsige = StringHelper.mergeMap(param);
        boolean result = certHelper.verify(unsige, param.get("sign"));
        if (!result) {
            log.error("验签失败", bgData);
            return null;
        }

        // 未开通交易接口发送邮件通知
        if (JixinResultContants.ERROR_JX900663.equalsIgnoreCase(t.getRetCode())) {
            exceptionEmailHelper.sendErrorMessage("访问权限受限, 需要联系即信", t.getTxCode());
        }
        t.setRetMsg(JixinResultContants.getMessage(t.getRetCode()));
        return t;
    }

    /**
     * 网络502
     */
    private static final String HTTP_CODE_502 = "502";
    /**
     * 网络504
     */
    private static final String HTTP_CODE_504 = "504";

    /**
     * 特殊回调
     *
     * @param request
     * @param typeToken
     * @param <T>
     * @return
     */
    public <T> T specialCallback(HttpServletRequest request, TypeToken<T> typeToken) {
        checkNotNull(request, "请求体为null");
        String bgData = request.getParameter("bgData");
        log.info(String.format("即信异步响应返回值：%s", bgData));
        checkNotNull(bgData, "返回值内容为空");
        T t = gson.fromJson(bgData, typeToken.getType());
        // 验证参数
        Map<String, String> param = gson.fromJson(bgData, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String unsige = StringHelper.mergeMap(param);
        boolean result = certHelper.verify(unsige, param.get("sign"));
        if (!result) {
            log.error("验签失败", bgData);
            return null;
        }
        return t;
    }


    public <T extends ContractBaseRequest, S extends ContractBaseResponse> S send(JixinTxCodeEnum txCodeEnum, T req, Class<S> clazz) {
        // 开启日子打印
        return baseSend(txCodeEnum, req, clazz, true);
    }


    public <T extends ContractBaseRequest, S extends ContractBaseResponse> S baseSend(JixinTxCodeEnum txCodeEnum, T req, Class<S> clazz, boolean openLogState) {
        checkNotNull(req, "请求体为null");
        // 前期初始化
        req.setInstCode(instCode);

        if (StringUtils.isEmpty(req.getSeqNo())) {
            req.setSeqNo(RandomHelper.generateNumberCode(6));
        }

        if (StringUtils.isEmpty(req.getTxTime())) {
            req.setTxTime(DateHelper.getTime());
        }

        if (StringUtils.isEmpty(req.getTxDate())) {
            req.setTxDate(DateHelper.getDateFor24());
        }

        if (StringUtils.isEmpty(req.getChannel())) {
            req.setChannel(ChannelContant.HTML);
        }

        req.setTxCode(txCodeEnum.getValue());
        String url = txCodeEnum.getUrl();
        String requestUrl = (url.contains("trimps")
                ? contentUrl
                : url.contains("getContracts") ? idsUrl : crtdataUrl) + url;
        String json = gson.toJson(req);
        //===============================
        // 加签
        //===============================
        Map<String, String> params = gson.fromJson(json, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String unSign = StringHelper.mergeMap(params);
        String sign = certHelper.doSign(unSign);
        params.put("sign", sign);
        if (openLogState) {
            log.info("=============================================");
            log.info(String.format("[%s]报文流水请求：%s%s%s", txCodeEnum.getName(), req.getTxDate(), req.getTxTime(), req.getSeqNo()));
            log.info("=============================================");
            log.info(String.format("即信请求报文: url=%s body=%s", requestUrl, gson.toJson(params)));
        }

        initHttps();
        HttpEntity entity = getHttpEntity(params);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<S> response = null;
        try {
            response = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, clazz);
        } catch (Throwable e) {
            log.error("请求即信服务器异常", e);
            exceptionEmailHelper.sendErrorMessage(String.format("请求即信异常: %s  %s", req.getTxCode(), e.getMessage()), gson.toJson(req));
            S s = null;
            try {
                s = clazz.newInstance();
            } catch (Exception ex) {
                log.error("实例化错误响应类失败", ex);
            }

            Preconditions.checkNotNull(s, "实例化错误响应失败");
            s.setResult(JixinResultContants.ERROR_COMMON_CONNECT);
            s.setMessage(String.format("请求网络异常: %s", e.getMessage()));

            // 针对 500以上代码特殊处理
            if (e instanceof HttpServerErrorException) {
                if (e.getMessage().contains(HTTP_CODE_502)) {
                    s.setResult(JixinResultContants.ERROR_502);
                    s.setMessage("请求存管系统, 网络返回502");
                } else if (e.getMessage().contains(HTTP_CODE_504)) {
                    s.setResult(JixinResultContants.ERROR_504);
                    s.setMessage("请求存管系统, 网络返回504");
                }
                return s;
            }

            return s;
        }
        checkNotNull(response, "即信响应类为空");
        S body = response.getBody();
        //==========================================
        // 参数验证
        //==========================================
        String bodyJson = gson.toJson(body);
        String contract = "contract";
        if (!url.contains(contract)) {
            Map<String, String> unverifyParams = gson.fromJson(bodyJson, TypeTokenContants.MAP_TOKEN);
            String unsige = StringHelper.mergeMap(unverifyParams);
            boolean result = certHelper.verify(unsige, unverifyParams.get("sign").toString());
            if (!result) {
                log.error("======================================");
                log.error(String.format("即信响应,签名验证失败, 数据[%s]", bodyJson));
                log.error("======================================");
                S s = null;
                try {
                    s = clazz.newInstance();
                } catch (Exception ex) {
                    log.error("实例化错误响应类失败", ex);
                }

                Preconditions.checkNotNull(s, "实例化错误响应失败");
                s.setResult(JixinResultContants.ERROR_SIGN);
                s.setMessage("非法访问, 验签失败");
                return s;
            }
            body.setMessage(JixinResultContants.getMessage(body.getResult()));

        }
        if (openLogState) {
            log.info("=============================================");
            log.info(String.format("[%s]报文流水响应：%s%s%s", txCodeEnum.getName(), req.getTxDate(), req.getTxTime(), req.getSeqNo()));
            log.info("=============================================");
            log.info(String.format("即信响应报文:url=%s body=%s", requestUrl, bodyJson));
        }

        /*
        // 未开通交易接口发送邮件通知
        if (JixinResultContants.ERROR_JX900663.equalsIgnoreCase(body.getResult())) {
            exceptionEmailHelper.sendErrorMessage("访问权限受限, 需要联系即信", gson.toJson(req));
        }*/

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