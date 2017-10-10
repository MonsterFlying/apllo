package com.gofobao.framework.helper;

import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zeke on 2017/6/1.
 */
@Component
@Slf4j
public class JixinHelper {

    @Autowired
    private DictItemService dictItemService;
    @Autowired
    private DictValueService dictValueService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;

    LoadingCache<String, DictValue> jixinCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemService.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    return dictValueService.findTopByItemIdAndValue01(dictItem.getId(), bankName);
                }
            });

    public static final String TENDER_PREFIX = "GFBT_";
    public static final String CANCEL_TENDER_PREFIX = "GFBCT_";
    public static final String LEND_REPAY_PREFIX = "GFBLR_";
    public static final String REPAY_PREFIX = "GFBR_";
    public static final String REPAY_BAIL_PREFIX = "GFBRB_";
    public static final String BAIL_REPAY_PREFIX = "GFBBR_";
    public static final String TENDER_CANCEL_PREFIX = "GFBTC_";
    public static final String BALANCE_FREEZE_PREFIX = "GFBBF_";
    public static final String BALANCE_UNFREEZE_PREFIX = "GFUBF_";
    public static final String END_CREDIT_PREFIX = "GFBEC_";


    public static String getOrderId(String prefix) {
        return prefix + new Date().getTime() + RandomHelper.generateNumberCode(9);
    }

    private static String oldBatchNo = null;

    /**
     * 获取6位批次号
     *
     * @return
     */
    public static String getBatchNo() {
        String nowBatchNo = DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_HMS_NUM);
        if (NumberHelper.toLong(oldBatchNo) > NumberHelper.toLong(nowBatchNo)) { //判断新生成的批次号是否小于以前的批次号
            nowBatchNo = oldBatchNo;
        }
        if (!ObjectUtils.isEmpty(oldBatchNo) && oldBatchNo.equals(nowBatchNo)) {
            nowBatchNo = String.valueOf(NumberHelper.toLong(nowBatchNo) + 1);
            if (nowBatchNo.length() < 6) { //判断批次号是否是5位数，如果是则补一个0
                nowBatchNo = "0" + nowBatchNo;
            }
        }
        oldBatchNo = nowBatchNo;
        return nowBatchNo;
    }

    /**
     * 获取名义账户
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
                DictValue dictValue = jixinCache.get("bailUserId");
                UserThirdAccount bailAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));
                return bailAccount.getAccountId();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取名义账户
     *
     * @return
     */
    public UserThirdAccount getTakeUserAccount() {

        try {
            DictValue dictValue = jixinCache.get("takeUserId");
            UserThirdAccount bailAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));
            return bailAccount;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取名义账户
     *
     * @return
     */
    public UserThirdAccount getTitularBorrowAccount() {
        try {
            DictValue dictValue = jixinCache.get("titularBorrowUserId");
            UserThirdAccount bailAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));
            return bailAccount;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取名义账户
     *
     * @param borrowId
     * @return
     */
    public UserThirdAccount getTitularBorrowAccount(long borrowId) {
        Borrow borrow = borrowService.findById(borrowId);
        if (ObjectUtils.isEmpty(borrow)) {
            return null;
        }

        String titularBorrowAccountId = borrow.getTitularBorrowAccountId();
        if (!ObjectUtils.isEmpty(titularBorrowAccountId)) {
            return userThirdAccountService.findByAccountId(titularBorrowAccountId);
        } else {
            try {
                DictValue dictValue = jixinCache.get("titularBorrowUserId");
                UserThirdAccount bailAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));
                return bailAccount;
            } catch (Throwable e) {
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
        return "G" + (char) (NumberHelper.toInt(prefix) + 64) + body;
    }

}
