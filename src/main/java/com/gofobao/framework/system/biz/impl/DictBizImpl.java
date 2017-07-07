package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.DictBiz;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.system.vo.response.VoServiceResp;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/6/20 0020.
 */
@Component
public class DictBizImpl implements DictBiz {

    @Autowired
    DictItemServcie dictItemServcie;


    @Autowired
    DictValueService dictValueService;

    LoadingCache<String, List<DictValue>> dictValueCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, List<DictValue>>() {
                @Override
                public List<DictValue> load(String s) throws Exception {
                    DictItem servicePlatform = dictItemServcie.findTopByAliasCodeAndDel(s, 0);
                    if (ObjectUtils.isEmpty(servicePlatform)) {
                        return null;
                    }
                    List<DictValue> dictValues = dictValueService.findByItemId(servicePlatform.getId());
                    if (ObjectUtils.isEmpty(dictValues)) {
                        return null;
                    }
                    return dictValues;
                }
            });

    @Override
    public ResponseEntity<VoServiceResp> service() {

        List<DictValue> dictValues = null;
        try {
            dictValues = dictValueCache.get("ABOUT_CODE");
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "获取服务信息失败", VoServiceResp.class));
        }
        VoServiceResp voServiceResp = VoBaseResp.ok("查询成功", VoServiceResp.class);
        dictValues.forEach(bean -> {
                    if (bean.getValue01().equals("servicePhoneHide")) {
                        voServiceResp.setServicePhoneHide(bean.getValue03());
                    } else if (bean.getValue01().equals("qqGroup")) {
                        voServiceResp.setQqGroup(bean.getValue03());
                    } else if (bean.getValue01().equals("workday")) {
                        voServiceResp.setWorkday(bean.getValue03());
                    } else if (bean.getValue01().equals("serviceQQ")) {
                        voServiceResp.setServiceQQ(bean.getValue03());
                    } else if (bean.getValue01().equals("serviceEmail")) {
                        voServiceResp.setServiceEmail(bean.getValue03());
                    } else if (bean.getValue01().equals("wechatCode")) {
                        voServiceResp.setWechatCode(bean.getValue03());
                    } else if (bean.getValue01().equals("servicePhoneView")) {
                        voServiceResp.setServicePhoneView(bean.getValue03());
                    } else if (bean.getValue01().equals("weiboCode")) {
                        voServiceResp.setWeiboCode(bean.getValue03());

                    }
                }
        );

        return ResponseEntity.ok(voServiceResp);
    }
}
