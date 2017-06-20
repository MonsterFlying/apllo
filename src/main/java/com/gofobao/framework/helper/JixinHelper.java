package com.gofobao.framework.helper;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictValueService;
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

    @Autowired
    private DictItemServcie dictItemServcie;
    @Autowired
    private DictValueService dictValueService;
    @Autowired
    private BorrowService borrowService;

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

                    return dictValueService.findTopByItemIdAndValue01(dictItem.getId(), bankName);
                }
            });

    public static final String TENDER_PREFIX = "GFBT_";
    public static final String LEND_REPAY_PREFIX = "GFBLP_";
    public static final String REPAY_PREFIX = "GFBP_";
    public static final String REPAY_BAIL_PREFIX = "GFBBP_";
    public static final String BAIL_REPAY_PREFIX = "GFBPB_";

    public static String getOrderId(String prefix) {
        return prefix + new Date().getTime();
    }

    public String getBatchNo() {

        DictItem dictItem = dictItemServcie.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);

        Date nowDate = new Date();
        int no = 10000;

        DictValue dictValue = dictValueService.findTopByItemIdAndValue01(dictItem.getId(), "firstCreateAt");
        if (!ObjectUtils.isEmpty(dictValue)) {
            Long firstCreateAt = NumberHelper.toLong(StringHelper.toString(dictValue.getValue03()));
            if (DateHelper.beginOfDate(nowDate).getTime() > firstCreateAt) {
                firstCreateAt = nowDate.getTime();
                dictValue.setValue03(StringHelper.toString(firstCreateAt));
                dictValue = dictValueService.findTopByItemIdAndValue01(dictItem.getId(), "no");
                dictValue.setValue03(StringHelper.toString(no));
                dictValueService.save(dictValue);
            } else {
                dictValue = dictValueService.findTopByItemIdAndValue01(dictItem.getId(), "no");
                no = NumberHelper.toInt(StringHelper.toString(dictValue.getValue03())) + 1;
                dictValue.setValue03(StringHelper.toString(no));
                dictValueService.save(dictValue);
            }
        }


        return StringHelper.toString(no);
    }

    /**
     * 获取担保账户
     *
     * @param borrowId
     * @return
     */
    public String getBailAccountId(long borrowId) {
        Borrow borrow = borrowService.findById(borrowId);
        if (ObjectUtils.isEmpty(borrow)) {
            return null;
        }

        String bailAccountId = borrow.getBailAccountId();
        if (!ObjectUtils.isEmpty(bailAccountId)) {
            return bailAccountId;
        } else {
            try {
                DictValue dictValue = jixinCache.get("bailAccountId");
                return dictValue.getValue03();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
