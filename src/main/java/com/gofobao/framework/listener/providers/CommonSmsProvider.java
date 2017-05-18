package com.gofobao.framework.listener.providers;

import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.message.service.SmsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Max on 2017/5/17.
 */
@Component
public class CommonSmsProvider {

    @Autowired
    SmsTemplateService smsTemplateService ;


    /**
     * 通过手机发送短信
     * @param phone
     * @param template
     * @param ip
     * @param extInfo
     * @return
     */
    public boolean doSend(String phone, String template, String ip, Map<String, String> extInfo){
        String code = RandomHelper.generateNumberCode(6); // 生成验证码
        Map<String, String> params = new HashMap<>() ;




        return true;
    }

}
