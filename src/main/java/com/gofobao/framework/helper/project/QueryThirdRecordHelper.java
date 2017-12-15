package com.gofobao.framework.helper.project;

import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Item;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Request;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Response;
import com.gofobao.framework.helper.DateHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class QueryThirdRecordHelper {

    @Autowired
    JixinManager jixinManager;

    public static final Gson GSON = new Gson();

    /**
     * 实时查询交易记录
     *
     * @param accountId 用户
     * @param date      查询时间
     * @param transType 交易类型
     * @return
     */
    public List<AccountDetailsQuery2Item> queryThirdRecord(String accountId,
                                                           Date date,
                                                           String transType) throws Exception {
        Date nowDate = new Date();
        Preconditions.checkArgument(!StringUtils.isEmpty(accountId), "accountId is empty");
        Preconditions.checkArgument(DateHelper.diffInDays(nowDate, date, false) < 2,
                "The inquiry time must be the same day");
        Preconditions.checkArgument(!StringUtils.isEmpty(transType), "transType is empty");

        List<AccountDetailsQuery2Item> accountDetailsQueryItemList = new ArrayList<>();
        String rtnInd = "";
        String inpDate = "";
        do {
            AccountDetailsQuery2Request accountDetailsQuery2Request = new AccountDetailsQuery2Request();
            accountDetailsQuery2Request.setRtnInd(rtnInd);
            accountDetailsQuery2Request.setInpDate(inpDate);
            accountDetailsQuery2Request.setStartDate(DateHelper.dateToString(date, DateHelper.DATE_FORMAT_YMD_NUM));
            accountDetailsQuery2Request.setEndDate(DateHelper.dateToString(date, DateHelper.DATE_FORMAT_YMD_NUM));
            accountDetailsQuery2Request.setType("9");
            accountDetailsQuery2Request.setTranType(transType);
            accountDetailsQuery2Request.setAccountId(accountId);
            // 循环次数
            int looper = 5;
            AccountDetailsQuery2Response accountDetailsQuery2Response = null;
            do {
                // 清除交易流水
                accountDetailsQuery2Request.setTxTime(null);
                accountDetailsQuery2Request.setTxDate(null);
                accountDetailsQuery2Request.setSeqNo(null);
                accountDetailsQuery2Response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY2,
                        accountDetailsQuery2Request,
                        AccountDetailsQuery2Response.class);

                // 判断请求异常执行,重试
                if (JixinResultContants.isBusy(accountDetailsQuery2Response)
                        || JixinResultContants.isNetWordError(accountDetailsQuery2Response)) {
                    log.warn("[查询即信交易流水] 系统繁忙/服务器错误");
                    --looper;
                    try {
                        Thread.sleep(1 * 1000L);
                    } catch (Exception e) {
                        log.error("[查询即信交易流水] 休眠异常", e);
                    }
                }
            } while (looper > 0);
            if ((ObjectUtils.isEmpty(accountDetailsQuery2Response))
                    || (!JixinResultContants.SUCCESS.equals(accountDetailsQuery2Response.getRetCode()))) {
                log.error("[查询即信交易流水] 严重错误");
                String msg = ObjectUtils.isEmpty(accountDetailsQuery2Response)
                        ? "当前网络出现异常, 请稍后尝试"
                        : accountDetailsQuery2Response.getRetMsg();
                throw new Exception(msg);
            }

            List<AccountDetailsQuery2Item> items = GSON.fromJson(accountDetailsQuery2Response.getSubPacks(),
                    new TypeToken<List<AccountDetailsQuery2Item>>() {
                    }.getType());
            Optional<List<AccountDetailsQuery2Item>> optional = Optional.ofNullable(items);

            List<AccountDetailsQuery2Item> accountDetailsQuery2Items = optional.orElse(Lists.newArrayList());
            inpDate = accountDetailsQuery2Items.get(accountDetailsQuery2Items.size() - 1).getInpDate();
            if (CollectionUtils.isEmpty(accountDetailsQuery2Items)) {
                break;
            }

            rtnInd = "1";
            accountDetailsQueryItemList.addAll(accountDetailsQuery2Items);
        } while (!CollectionUtils.isEmpty(accountDetailsQueryItemList));
        return accountDetailsQueryItemList;
    }
}
