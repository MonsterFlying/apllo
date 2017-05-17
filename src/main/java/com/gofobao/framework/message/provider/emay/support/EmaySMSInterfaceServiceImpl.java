package com.gofobao.framework.message.provider.emay.support;

import com.gofobao.common.sms.AbstractSMSConfig;
import com.gofobao.common.sms.SMSInterfaceService;
import com.gofobao.common.sms.emay.httpclient.SDKHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 亿美短信发送实现类
 * Created by Max on 17/2/16.
 */
@Service
public class EmaySMSInterfaceServiceImpl implements SMSInterfaceService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 短信发送成功标识
     */
    public static final String SEND_SMS_SUCCESS = "0";


    @Override
    public boolean sendMessage(AbstractSMSConfig config, List<String> phones, String message) throws Exception {
        if ((config == null) || (phones == null) || (StringUtils.isEmpty(message))) {
            if (logger.isDebugEnabled()) {
                logger.debug("发送短信参数不存在");
            }

            throw new Exception("发送短信参数不存在");
        }

        EmaySMSConfig emayConfig = (EmaySMSConfig) config;
        StringBuffer phoneSB = new StringBuffer();

        // 处理群发情况
        Iterator<String> iterator = phones.iterator();
        phoneSB.append(iterator.next());
        while (iterator.hasNext()) {
            phoneSB.append(",").append(iterator.next());
        }

        // 短信拼接
        message = URLEncoder.encode(message, "UTF-8");
        String code = "";
        long seqId = System.currentTimeMillis();
        String url = emayConfig.getBaseUrl() + "sendsms.action";
        String ret = null;

        Map<String, String> params = new HashMap<>();
        params.put("cdkey", emayConfig.getSn());
        params.put("password", emayConfig.getKey());
        params.put("phone", phoneSB.toString());
        params.put("message", message);
        params.put("addserial", code);
        params.put("seqid", new Long(seqId).toString());

        // 发送短信
        try {
            ret = SDKHttpClient.sendSMS(url, params);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("亿美短信发送失败：" + e.getMessage());
            }

            throw e;
        }

        return SEND_SMS_SUCCESS.equals(ret);
    }
}
