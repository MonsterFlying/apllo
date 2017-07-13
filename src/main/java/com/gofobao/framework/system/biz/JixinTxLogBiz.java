package com.gofobao.framework.system.biz;

import com.gofobao.framework.api.helper.JixinTxCodeEnum;

import java.util.Map;

/**
 * Created by Administrator on 2017/7/13 0013.
 */
public interface JixinTxLogBiz {

    /**
     * 保存请求体
     * @param jixinTxCodeEnum
     * @param request
     */
    void saveRequest(JixinTxCodeEnum jixinTxCodeEnum, Map<String, String> request)  ;

    /**
     * 保存响应体
     * @param jixinTxCodeEnum
     * @param response
     */
    void saveResponse(Map<String, String> response)  ;
}
