package com.gofobao.framework.helper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/6/23 0023.
 */
@Component
@Slf4j
public class BankBinHelper {

    @Value(value = "classpath:bank/bankBin.json")
    private Resource bankBinJson;

    ConcurrentMap<String, BankInfo> stringBankInfoConcurrentMap =  new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        Gson gson = new Gson() ;
        InputStream inputStream = bankBinJson.getInputStream();
        JsonParser jsonParser = new JsonParser();
        JsonElement parse = jsonParser.parse(new JsonReader(new InputStreamReader(inputStream)));
        JsonArray root = parse.getAsJsonArray();
        BankInfo bankInfo = null ;
        String cardId = null ;
        for(int index = 0 ,size = root.size(); index < size; index++){
            JsonElement jsonElement = root.get(index);
            bankInfo =  gson.fromJson(jsonElement, BankInfo.class);
            cardId = bankInfo.getCardId();
            stringBankInfoConcurrentMap.put(cardId, bankInfo) ;
        }

        log.info(String.format("银行bin初始化完成, 总数: %s", stringBankInfoConcurrentMap.size()));
    }

    /**
     * 根据银行卡获取用户信息
     * @param cardNo
     * @return
     */
    public BankInfo find(String cardNo) {
        Set<String> strings = stringBankInfoConcurrentMap.keySet();
        Iterator<String> iterator = strings.iterator();
        String next = null ;
        while (iterator.hasNext()){
            next = iterator.next();
            if(cardNo.indexOf(next) == 0){
                return stringBankInfoConcurrentMap.get(next) ;
            }
        }

        return null ;
    }


    @Data
    public static class BankInfo{
        private String bankName;
        private String bankOrgNo;
        private String cardName ;
        private String cardType ;
        private String cardLength ;
        private String cardPrdLength ;
        private String cardId ;
    }
}
