package com.gofobao.framework.api;

import com.gofobao.framework.api.helper.jixin.SignUtil;
import com.gofobao.framework.api.request.AbsRequest;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.OKHttpHelper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/17.
 */
@Component
public class OpenHttp {

    private static final Gson gson = new GsonBuilder().create();

    @Value("${jixin.domain}")
    private String testDomain;
    @Value("${jixin.version}")
    private String version;
    @Value("${jixin.txCode}")
    private String txCode;
    @Value("${jixin.instCode}")
    private String instCode;
    @Value("${jixin.bankCode}")
    private String bankCode;

    private void init(AbsRequest absRequest){
        Date nowDate = new Date();
        absRequest.setInstCode(instCode);
        absRequest.setBankCode(bankCode);
        absRequest.setTxCode(txCode);
        absRequest.setVersion(version);
        absRequest.setTxDate(DateHelper.dateToString(nowDate,DateHelper.DATE_FORMAT_YMD_NUM));
        absRequest.setTxTime(DateHelper.dateToString(nowDate,DateHelper.DATE_FORMAT_HMS_NUM));
    }

    /**
     * 发送请求
     *
     * @param method 请求方法
     * @param absRequest
     * @return
     */
    public Map<String, String> sendHttp(String method, AbsRequest absRequest) throws Exception{

        init(absRequest);//初始化请求参数

        Map<String, String> reqMap = gson.fromJson(gson.toJson(absRequest), new TypeToken<Map<String, String>>() {
        }.getType());
        reqMap.put("sign",SignUtil.sign(absRequest));

        String bodyJson = OKHttpHelper.postJson(testDomain + method,gson.toJson(reqMap),null );
        if (!SignUtil.verify(bodyJson)){
            throw new Exception("验签失败！");
        }

        return null;
    }
}
