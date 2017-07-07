package com.gofobao.framework.listener.providers;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MacthHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.message.entity.SmsEntity;
import com.gofobao.framework.message.provider.SmsServerConfig;
import com.gofobao.framework.message.repository.SmsRepository;
import com.gofobao.framework.message.service.SmsConfigService;
import com.gofobao.framework.message.service.SmsTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Max on 2017/5/17.
 */
@Component
@Slf4j
public class CommonSmsProvider {

    @Autowired
    SmsTemplateService smsTemplateService ;

    @Autowired
    SmsConfigService smsConfigService;

    @Autowired
    SmsRepository smsRepository ;

    @Autowired
    MacthHelper macthHelper;

    @Value("${gofobao.close-phone-send}")
    boolean closePhoneSend ;

    public static final String TEMPLATE_KEY_SMSCODE = "smscode";
    public static final String TEMPLATE_KEY_TIMESTAMP = "timestamp";


    /**
     * 根据制定手机发送验证码
     * @param tag 类型
     * @param body 请求内容
     * @return 发送是否成功
     */
    public boolean doSendMessageCode(String tag, Map<String, String> body){
        checkNotNull(body,  "CommonSmsProvider doSendMessageCode body is null") ;
        String phone = body.get(MqConfig.PHONE);
        String ip = body.get(MqConfig.IP) ;

        checkNotNull(phone, "CommonSmsProvider doSendMessageCode phone is null") ;
        checkNotNull(ip, "CommonSmsProvider doSendMessageCode ip is null") ;

        // 获取模板
        String template = smsTemplateService.findSmsTemplate(tag);
        checkNotNull(template, "CommonSmsProvider doSendMessageCode template is null") ;

        // 获取随机验证码
        String code = RandomHelper.generateNumberCode(6); // 生成验证码
        if(closePhoneSend){
            code = "111111" ;
        }

        log.info(String.format("验证码: %s", code));
        Map<String, String> params = new HashMap<>() ;
        params.put(TEMPLATE_KEY_SMSCODE, code) ;
        params.put(TEMPLATE_KEY_TIMESTAMP, DateHelper.dateToString(new Date()));
        params.putAll(body);

        String message = replateTemplace(template, params);  // 替换短信模板
        List<String> phones = new ArrayList<>(1);
        phones.add(phone) ;

        SmsServerConfig smsServerConfig = smsConfigService.installSMSServer();  // 获取短信配置
        if(ObjectUtils.isEmpty(smsServerConfig)){
            return false ;
        }

        boolean rs = false;
        try {
            if(!closePhoneSend){
                smsServerConfig.getService().
                        sendMessage(smsServerConfig.getConfig(), phones, message);
            }

            rs = true ;
        } catch (Throwable e) {
            log.error("CommonSmsProvider doSendMessageCode send message error", e);
            return false;
        }

        //  写入缓存
        if(rs){
            try {
                macthHelper.add(tag, phone, code) ;
            } catch (Throwable e) {
                log.error("CommonSmsProvider doSendMessageCode put redis error", e);
                return false;
            }
        }

        // 写入数据库
        Date nowDate = new Date() ;
        SmsEntity smsEntity = new SmsEntity();
        smsEntity.setIp(ip) ;
        smsEntity.setType(tag) ;
        smsEntity.setContent(message) ;
        smsEntity.setPhone(phone) ;
        smsEntity.setCreatedAt(nowDate) ;
        smsEntity.setStatus(rs? 0: 1) ;
        smsEntity.setUsername(phone) ;
        smsEntity.setExt(" ");
        smsEntity.setId(null);
        smsEntity.setRrid(" ");
        smsEntity.setStime(" ");
        try {
            smsRepository.save(smsEntity);
        }catch (Throwable e){
            log.error("保存数据失败", e);
        }
        return rs ;
    }


    /**
     * 替换模板
     *
     * @param template 魔板
     * @param params   替换参数
     * @return 替换后模板
     */
    public static String replateTemplace(String template, Map<String, String> params) {
        return StringHelper.replateTemplace(template, "{", params, "}");
    }

}
