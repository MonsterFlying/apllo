package com.gofobao.framework.message.service;

/**
 *  短信模板
 * Created by Max on 17/5/18.
 */
public interface SmsTemplateService {

    /**
     * 发现短信模板
     * @param alias 别名
     * @return 模板
     */
    String findSmsTemplate(String alias) ;
}
