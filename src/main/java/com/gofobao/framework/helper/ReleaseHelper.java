package com.gofobao.framework.helper;

import com.gofobao.framework.helper.project.SecurityHelper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Slf4j
public class ReleaseHelper {
    /**
     * 发送代码
     *
     * @param url  url路径
     * @param data 数据
     * @return
     */
    public static boolean sendMsg(String url, Map<String, String> data) {
        try {
            MultiValueMap<String, String> stringStringMap = SecurityHelper.doSign(data);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            //  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(stringStringMap, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("========================================");
            log.info("发送结果:" + new Gson().toJson(response));
            log.info("========================================");
            return true;
        } catch (Exception e) {
            log.error("发送通信失败", e);
            return false;
        }

    }
}
