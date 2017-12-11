package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.JacksonHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.listener.providers.FinancePlanProvider;
import com.gofobao.framework.listener.providers.ProductProvider;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Zeke on 2017/8/14.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_PRODUCT)
public class ProductListener {

    @Autowired
    private ProductProvider productProvider;

    static Gson GSON = new GsonBuilder().create();

    @RabbitHandler
    public void process(String message) {

        Preconditions.checkNotNull(message, "ProductListener process message is empty");
        Map<String, Object> body = GSON.fromJson(message, TypeTokenContants.MAP_TOKEN);
        Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "ProductListener process tag is empty ");
        Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "ProductListener process body is empty ");
        String tag = body.get(MqConfig.MSG_TAG).toString();
        Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);

        if (tag.equals(MqTagEnum.GENERATE_PRODUCT_PLAN.getValue())) {  // 生成商品计划
            try {
                productProvider.generateProductPlan(msg);
            } catch (Throwable throwable) {
                log.error("商品计划生成理财计划异常:", throwable);
            }
        } else {
            log.error("AutoTenderListener 未找到对应的type");
        }
    }

}
