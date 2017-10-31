package com.gofobao.framework.helper.project;

import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.as.biz.impl.RechargeStatementBizImpl;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
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
    public List<AccountDetailsQueryItem> queryThirdRecord(String accountId,
                                                          Date date,
                                                          String transType) throws Exception {
        Date nowDate = new Date();
        Preconditions.checkArgument(!StringUtils.isEmpty(accountId), "accountId is empty");
        Preconditions.checkArgument(DateHelper.diffInDays(nowDate, date, false) < 2,
                "The inquiry time must be the same day");
        Preconditions.checkArgument(!StringUtils.isEmpty(transType), "transType is empty");

        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>();
        int pageSize = 20, pageIndex = 1, realSize = 0;
        do {
            AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
            accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
            accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
            accountDetailsQueryRequest.setStartDate(DateHelper.dateToString(date, DateHelper.DATE_FORMAT_YMD_NUM));
            accountDetailsQueryRequest.setEndDate(DateHelper.dateToString(date, DateHelper.DATE_FORMAT_YMD_NUM));
            accountDetailsQueryRequest.setType("9");
            accountDetailsQueryRequest.setTranType(transType);
            accountDetailsQueryRequest.setAccountId(accountId);
            // 循环次数
            int looper = 5;
            AccountDetailsQueryResponse accountDetailsQueryResponse = null;
            do {
                // 清除交易流水
                accountDetailsQueryRequest.setTxTime(null);
                accountDetailsQueryRequest.setTxDate(null);
                accountDetailsQueryRequest.setSeqNo(null);
                accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                        accountDetailsQueryRequest,
                        AccountDetailsQueryResponse.class);

                // 判断请求异常执行,重试
                if (JixinResultContants.isBusy(accountDetailsQueryResponse)
                        || JixinResultContants.isNetWordError(accountDetailsQueryResponse)) {
                    log.warn("[查询即信交易流水] 系统繁忙/服务器错误");
                    --looper;
                    try {
                        Thread.sleep(1 * 1000L);
                    } catch (Exception e) {
                        log.error("[查询即信交易流水] 休眠异常", e);
                    }
                }
            } while (looper > 0);
            if ((ObjectUtils.isEmpty(accountDetailsQueryResponse))
                    || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
                log.error("[查询即信交易流水] 严重错误");
                String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse)
                        ? "当前网络出现异常, 请稍后尝试"
                        : accountDetailsQueryResponse.getRetMsg();
                throw new Exception(msg);
            }

            pageIndex++;
            List<AccountDetailsQueryItem> items = GSON.fromJson(accountDetailsQueryResponse.getSubPacks(),
                    new TypeToken<List<AccountDetailsQueryItem>>() {
                    }.getType());
            Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(items);

            List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList());
            if (CollectionUtils.isEmpty(accountDetailsQueryItems)) {
                break;
            }

            realSize = accountDetailsQueryItems.size();
            accountDetailsQueryItemList.addAll(accountDetailsQueryItems);
        } while (realSize == pageSize);
        return accountDetailsQueryItemList;
    }
}
