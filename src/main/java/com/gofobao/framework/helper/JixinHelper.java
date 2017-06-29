package com.gofobao.framework.helper;

import com.gofobao.framework.api.contants.IdTypeContant;
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
        int noNum = 100000;

        DictValue createAt = dictValueService.findTopByItemIdAndValue01(dictItem.getId(), "firstCreateAt");
        DictValue no = null;
        if (!ObjectUtils.isEmpty(createAt)) {
            Long firstCreateAt = NumberHelper.toLong(StringHelper.toString(createAt.getValue03()));
            if (DateHelper.beginOfDate(nowDate).getTime() > firstCreateAt) {
                firstCreateAt = nowDate.getTime();
                createAt.setValue03(StringHelper.toString(firstCreateAt));
                dictValueService.save(createAt);

                no = dictValueService.findTopByItemIdAndValue01(dictItem.getId(), "no");
                no.setValue03(StringHelper.toString(noNum));
                dictValueService.save(no);
            } else {
                no = dictValueService.findTopByItemIdAndValue01(dictItem.getId(), "no");
                noNum = NumberHelper.toInt(StringHelper.toString(no.getValue03())) + 1;
                no.setValue03(StringHelper.toString(noNum));
                dictValueService.save(no);
            }
        }


        return StringHelper.toString(noNum);
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

    /**
     * 获取身份证类型
     *
     * @param type
     * @return
     */
    public static String getIdType(int type) {
        String idType = null;
        switch (type) {
            case 1:
                idType = IdTypeContant.ID_CARD;
                break;
            default:
        }
        return idType;
    }

    /**
     * s生成标的号
     */
    public String generateProductId(Long borrowId) {
        String str = String.valueOf(borrowId);
        int index = str.length() - 5;
        String body = str.substring(index, str.length());
        String prefix = str.substring(0, index);
        return (char) (NumberHelper.toInt(prefix) + 64) + body;
    }

    public static void main(String[] args) {

        System.out.println((char) 65);
    }

}
