package com.gofobao.framework.scheduler;


import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelRequest;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelResponse;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.financial.biz.FinancialSchedulerBiz;
import com.gofobao.framework.financial.entity.FinancialScheduler;
import com.gofobao.framework.financial.service.FinancialSchedulerService;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.biz.impl.BrokerBounsBizImpl;
import com.gofobao.framework.member.entity.BrokerBouns;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.BrokerBounsService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserBonusScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BrokerBounsService brokerBounsService;

    @Autowired
    private BrokerBounsBizImpl brokerBounsBiz;

    @Autowired
    private AssetChangeProvider assetChangeProvider;

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Autowired
    private JixinManager jixinManager;

    @Autowired
    private FinancialSchedulerBiz financialSchedulerBiz;

    @Autowired
    private NewAssetLogService newAssetLogService;

    @Autowired
    private RedPackageBiz redPackageBiz;

    /**
     * 理财师提成
     */
    @Scheduled(cron = "0 35 23 * * ? ")
    public void brokerProcess() {
        Date nowDate = new Date();

    }

    /**
     * 天提成
     */
    @Scheduled(cron = "0 30 23 * * ? ")
    public void dayProcess() {
        Date nowDate = new Date();
        brokerBounsBiz.dayPushMoney(nowDate);
    }

    /**
     * 月提成
     */
    @Scheduled(cron = "0 35 23 1 * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void monthProcess() {
        Date nowDate = new Date();


    }
}
