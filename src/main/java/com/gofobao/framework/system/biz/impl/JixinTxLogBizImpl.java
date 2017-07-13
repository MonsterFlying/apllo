package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.system.biz.JixinTxLogBiz;
import com.gofobao.framework.system.entity.JixinTxLog;
import com.gofobao.framework.system.service.JixinTxLogService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/13 0013.
 */

@Service
@Slf4j
public class JixinTxLogBizImpl implements JixinTxLogBiz {

    @Value("${gofobao.close-jixin-log}")
    private boolean closeState;


    Gson gson = new Gson();

    @Autowired
    JixinTxLogService jixinTxLogService;


    @Override
    public void saveRequest(JixinTxCodeEnum jixinTxCodeEnum, Map<String, String> request) {
        if(closeState){
           return;
        }
        try {
            JixinTxLog jixinTxLog = new JixinTxLog();
            jixinTxLog.setTxType(jixinTxCodeEnum.getValue());
            jixinTxLog.setTxTypeDesc(jixinTxCodeEnum.getName());
            jixinTxLog.setSeqNo(String.format("%s%s%s", request.get("txDate"), request.get("txTime"), request.get("seqNo")));
            jixinTxLog.setCreateAt(new Date());
            jixinTxLog.setType(0);
            jixinTxLog.setBody(gson.toJson(request));
            jixinTxLogService.save(jixinTxLog);
        } catch (Throwable e) {
            log.error("保存请求体失败", e);
        }

    }

    @Override
    public void saveResponse(Map<String, String> response) {
        if(closeState){
            return;
        }
        try {
            String txCode = response.get("txCode");
            String txDes = null ;
            JixinTxCodeEnum jixinTxCodeEnum = null ;
            JixinTxCodeEnum[] values = JixinTxCodeEnum.values();
            for(JixinTxCodeEnum bean : values){
                if(bean.getValue().equals(txCode)){
                    jixinTxCodeEnum = bean;
                }
            }

            JixinTxLog jixinTxLog = new JixinTxLog();
            jixinTxLog.setTxType(jixinTxCodeEnum.getValue());
            jixinTxLog.setTxTypeDesc(jixinTxCodeEnum.getName());
            jixinTxLog.setSeqNo(String.format("%s%s%s", response.get("txDate"), response.get("txTime"), response.get("seqNo")));
            jixinTxLog.setCreateAt(new Date());
            jixinTxLog.setType(1);
            jixinTxLog.setBody(gson.toJson(response));
            jixinTxLogService.save(jixinTxLog);
        } catch (Throwable e) {
            log.error("保存响应体失败", e);
        }
    }


}
