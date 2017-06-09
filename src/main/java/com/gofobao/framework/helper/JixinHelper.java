package com.gofobao.framework.helper;

import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictService;
import com.gofobao.framework.system.service.DictValueServcie;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zeke on 2017/6/1.
 */
@Component
public class JixinHelper {
    /**
     * 今天第一产生批次号时间
     */
    private static Long firstCreateAt;
    /**
     * 批次编号 100000开始
     */
    private static int no;
    @Autowired
    private DictItemServcie dictItemServcie;
    @Autowired
    private DictValueServcie dictValueServcie;

    LoadingCache<String, DictValue> jixinCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemServcie.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    return dictValueServcie.findTopByItemIdAndValue01(dictItem.getId(), bankName);
                }
            });

    public static final String TENDER_PREFIX = "GFBT_";
    public static final String LEND_PAY_PREFIX = "GFBLP_";

    public static String getOrderId(String prefix) {
        return prefix + new Date().getTime();
    }

    public String getBatchNo() {
        Date nowDate = new Date();
        if (ObjectUtils.isEmpty(firstCreateAt)) {
            try {
                DictValue dictValue = jixinCache.get("firstCreateAt");
                if (!ObjectUtils.isEmpty(dictValue)) {
                    firstCreateAt = NumberHelper.toLong(StringHelper.toString(dictValue.getValue03()));
                    if (DateHelper.beginOfDate(nowDate).getTime() > firstCreateAt) {
                        firstCreateAt = nowDate.getTime();
                        no = 100000;
                        dictValue.setValue03(StringHelper.toString(firstCreateAt));
                        jixinCache.put("firstCreateAt", dictValue);
                        dictValueServcie.save(dictValue);
                    }else {
                        dictValue = jixinCache.get("no");
                        no = NumberHelper.toInt(StringHelper.toString(dictValue.getValue03()));
                    }
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return StringHelper.toString(no++);
    }


}
