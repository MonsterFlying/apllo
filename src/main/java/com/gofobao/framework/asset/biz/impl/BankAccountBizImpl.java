package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.vo.response.VoBankTypeInfoResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictValueServcie;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
@Slf4j
public class BankAccountBizImpl implements BankAccountBiz{

    @Value("${gofobao.aliyun-bankinfo-url}")
    String aliyunQueryBankUrl ;

    @Value("${gofobao.aliyun-bankinfo-appcode}")
    String aliyunQueryAppcode ;

    @Autowired
    DictValueServcie dictValueServcie ;

    @Autowired
    DictItemServcie dictItemServcie ;

    @Autowired
    UserThirdAccountService userThirdAccountService ;

    LoadingCache<String, DictValue> bankLimitCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemServcie.findTopByAliasCodeAndDel("PLATFORM_BANK", 0) ;
                    if(ObjectUtils.isEmpty(dictItem)){
                        return null ;
                    }

                    return dictValueServcie.findTopByItemIdAndValue02(dictItem.getId(), bankName);
                }
            }) ;


    @Override
    public ResponseEntity<VoBankTypeInfoResp> findTypeInfo(Long userId, String account) {
        if( StringUtils.isEmpty(account) ){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前银行账号为空", VoBankTypeInfoResp.class)) ;
        }

        UserThirdAccount thirdAccount = userThirdAccountService.findByUserId(userId);
        if( (!ObjectUtils.isEmpty(thirdAccount)) && (!StringUtils.isEmpty(thirdAccount.getCardNo()))){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前账户已经绑定银行卡！", VoBankTypeInfoResp.class)) ;
        }

        // 判断当前银行卡是否存在
        UserThirdAccount userThirdAccount = userThirdAccountService.findTopByCardNo(account) ;

        if(!ObjectUtils.isEmpty(userThirdAccount)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前银行卡已被使用", VoBankTypeInfoResp.class)) ;
        }

        // 请求支付宝获取银行信息
        Map<String, String> params = new HashMap<>();
        params.put("bankcard", account);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", String.format("APPCODE %s", aliyunQueryAppcode));
        String jsonStr = OKHttpHelper.get(aliyunQueryBankUrl, params, headers);

        if (StringUtils.isEmpty(jsonStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候重试", VoBankTypeInfoResp.class)) ;
        }

        JsonObject result = new JsonParser().parse(jsonStr).getAsJsonObject();
        int status = result.get("status").getAsInt();
        if (status != 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候重试", VoBankTypeInfoResp.class)) ;
        }

        JsonObject info = result.get("result").getAsJsonObject();
        String type = info.get("type").getAsString();
        if (!"借记卡".equals(type)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "平台暂不支持信用卡", VoBankTypeInfoResp.class)) ;
        }
        String bankname = info.get("bank").getAsString();

        // 获取银行
        DictValue bank = null;
        try {
            bank = bankLimitCache.get(bankname);
        } catch (ExecutionException e) {
            log.error("BankAccountBizImpl.findTypeInfo: bank type is exists ");
        }
        if(ObjectUtils.isEmpty(bank)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("平台暂不支持%s, 请尝试其他银行卡！", bankname), VoBankTypeInfoResp.class)) ;
        }

        // 返回银行信息
        VoBankTypeInfoResp resp = VoBaseResp.ok("查询成功", VoBankTypeInfoResp.class);
        resp.setBankName(bankname);
        resp.setTimesLimitMoney(bank.getValue04());
        resp.setDayLimitMoney(bank.getValue05());
        resp.setMonthLimitMonty(bank.getValue06());
        resp.setBankIcon(info.get("logo").getAsString());

        return ResponseEntity.ok(resp) ;

    }
}
