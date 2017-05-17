package com.gofobao.framework.message.provider.emay.httpclient;


import com.gofobao.framework.helper.OKHttpHelper;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.util.Map;


public class SDKHttpClient {
    static Logger logger = Logger.getLogger(SDKHttpClient.class);


    public static String sendSMS(String url, Map<String, String> params) {
        String response = null;
        String rs = "";
        try {
            response = OKHttpHelper.get(url, params, null);
        } catch (Exception e) {
            logger.error(String.format("请求发送出现异常: %s", e.getMessage()));
            return rs;
        }

        if (StringUtils.isEmpty(response)) {
            rs = xmlMt(response);
        }

        return rs;
    }

    // 解析下发response
    public static String xmlMt(String response) {
        String result = "0";
        Document document = null;
        try {
            document = DocumentHelper.parseText(response);
        } catch (DocumentException e) {
            e.printStackTrace();
            result = "-250";
        }
        Element root = document.getRootElement();
        result = root.elementText("error");
        if (null == result || "".equals(result)) {
            result = "-250";
        }
        return result;
    }
}
