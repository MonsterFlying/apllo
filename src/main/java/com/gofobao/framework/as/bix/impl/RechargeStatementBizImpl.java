package com.gofobao.framework.as.bix.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.as.bix.RechargeStatementBiz;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.financial.entity.NewAleve;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Component
@Slf4j
public class RechargeStatementBizImpl implements RechargeStatementBiz {
    @Autowired
    private UserService userService;

    @Autowired
    private RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    NewEveService newEveService;

    @Autowired
    NewAleveService newAleveService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    /**
     * 1. 查询该用户指定某天的所有第三方充值记录
     * 注意: 判断对账时间是否隔天, 隔天直接查询DB, 否则实时查询.
     * 2. 查询该用户所有充值记录
     * 3. 逐个匹配金额
     */
    @Override
    public boolean matchOfflineRecharge(Long userId, Date date) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "userthirdAccount record is null");
        Preconditions.checkNotNull(date, "date is null");
        List<NewEve> thirdRechargeRecord = null;
        try {
            thirdRechargeRecord = findThirdRechargeRecord(userThirdAccount, date, RechargeType.offlineRecharge);
        } catch (Exception e) {
            log.warn("对账: 查询线下充值记录异常", e);
            return false;
        }

        List<RechargeDetailLog> rechargeDetailLogs = findLocalRechargeRecord(userThirdAccount, date, RechargeType.offlineRecharge);

        return false;
    }


    /**
     * 查询本地充值流水
     *
     * @param userThirdAccount 用户类型
     * @param date             查询时间
     * @param offlineRecharge  充值类型
     * @return
     */
    private List<RechargeDetailLog> findLocalRechargeRecord(UserThirdAccount userThirdAccount,
                                                            Date date,
                                                            RechargeType offlineRecharge) {
        Date beginDate = DateHelper.endOfDate(DateHelper.subDays(date, 1)); //  查询开始时间
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(date, 1)); // 查询结束时间

        Specification<RechargeDetailLog> rechargeDetailLogSpecification = Specifications
                .<RechargeDetailLog>and()
                .eq("userId", userThirdAccount.getUserId())
                .in("createTime", new Range(beginDate, endDate))
                .build();

        List<RechargeDetailLog> rechargeDetailLogs = rechargeDetailLogService.findAll(rechargeDetailLogSpecification);
        Optional<List<RechargeDetailLog>> optinal = Optional.ofNullable(rechargeDetailLogs);
        return optinal.orElse(Lists.newArrayList());
    }

    @Override
    public boolean matchOnlineRecharge(Long userId, Date date) {
        return false;
    }


    public enum RechargeType {
        offlineRecharge("7820"), onlineRecharge("7822");

        RechargeType(String type) {
            this.type = type;
        }

        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private final static Gson gson = new Gson();


    /**
     * 查询第三方充值记录
     *
     * @param userThirdAccount 用户账户
     * @param date             查询日期
     * @param rechargeType     充值类型
     * @return
     */
    private List<NewEve> findThirdRechargeRecord(UserThirdAccount userThirdAccount,
                                                 Date date,
                                                 RechargeType rechargeType) throws Exception {
        Date nowDate = new Date();
        if (DateHelper.diffInDays(nowDate, DateHelper.beginOfDate(date), false) != 0) {
            // 此处隔天, 直接查询eve对账文件
            Specification<NewEve> newEveSpecification = Specifications
                    .<NewEve>and()
                    .eq("cardnbr", userThirdAccount.getAccountId())
                    .eq("transtype", rechargeType.getType())
                    .eq("queryTime", DateHelper.dateToString(date, DateHelper.DATE_FORMAT_YMD_NUM))
                    .build();
            List<NewEve> eveLists = newEveService.findAll(newEveSpecification);
            Optional<List<NewEve>> result = Optional.ofNullable(eveLists);
            return result.orElse(Lists.newArrayList());
        } else {
            throw new Exception("eve 只能查询隔天充值记录");
        }
    }


}
