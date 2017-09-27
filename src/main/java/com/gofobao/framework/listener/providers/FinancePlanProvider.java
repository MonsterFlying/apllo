package com.gofobao.framework.listener.providers;

import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Zeke on 2017/8/14.
 */
@Slf4j
@Component
public class FinancePlanProvider {
    static Gson GSON = new GsonBuilder().create();

    @Value("${gofobao.adminDomain}")
    private String adminDomain;

    /**n
     * 理财计划满标后通知
     */
    public void pullScaleNotify(Map<String, Object> msg) {
        try {
            Map<String, String> requestMaps = ImmutableMap.of("paramStr",GSON.toJson(msg),"sign",SecurityHelper.getSign(GSON.toJson(msg)));
            String resultStr=OKHttpHelper.postForm(adminDomain+"/api/open/finance-plan/review",requestMaps, null);
            System.out.print("返回响应:"+resultStr);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

}
