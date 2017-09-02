package com.gofobao.framework.asset.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.asset.biz.AssetSynBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.financial.entity.Aleve;
import com.gofobao.framework.financial.service.AleveService;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class AssetSynBizImpl implements AssetSynBiz {
    @Autowired
    AssetLogService assetLogService;

    @Autowired
    AssetService assetService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    UserService userService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    MqHelper mqHelper;

    @Autowired
    AleveService aleveService;

    public static final Gson GSON = new Gson();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doAssetSyn(Long userId) throws Exception {
        Users user = userService.findByIdLock(userId);
        Preconditions.checkNotNull(user, "资金同步: 查询用户为空");
        Asset asset = assetService.findByUserIdLock(userId);  // 用户资产
        Preconditions.checkNotNull(asset, "资金同步: 查询用户资产为空");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        ResponseEntity<VoBaseResp> conditionResponse = ThirdAccountHelper.allConditionCheck(userThirdAccount);
        if (!conditionResponse.getStatusCode().equals(HttpStatus.OK)) {
            return false;
        }

        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            String msg = ObjectUtils.isEmpty(balanceQueryResponse) ? "当前网络异常, 请稍后尝试!" : balanceQueryResponse.getRetMsg();
            log.error(String.format("资金同步: %s", msg));
            return false;
        }

        double currBal = MathHelper.myRound(NumberHelper.toDouble(balanceQueryResponse.getCurrBal()) * 100.0, 2);  // 账户总额
        if (new Double(currBal * 100).longValue() <= (asset.getNoUseMoney() + asset.getUseMoney())) {
            return false;
        }

        // 用户资金记录查询
        int pageSize = 20, pageIndex = 1, realSize = 0;
        String accountId = userThirdAccount.getAccountId();  // 存管账户ID
        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>();
        do {
            AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
            accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
            accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
            accountDetailsQueryRequest.setStartDate(jixinTxDateHelper.getTxDateStr()); // 查询当天数据
            accountDetailsQueryRequest.setEndDate(jixinTxDateHelper.getTxDateStr());
            accountDetailsQueryRequest.setType("9");
            accountDetailsQueryRequest.setTranType("7820"); //  线下转账
            accountDetailsQueryRequest.setAccountId(accountId);

            AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                    accountDetailsQueryRequest,
                    AccountDetailsQueryResponse.class);

            if ((ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg();
                log.error(String.format("资金同步失败: %s", msg));
                return false;
            }
            pageIndex++;
            Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
            }.getType()));
            List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList());
            if (CollectionUtils.isEmpty(accountDetailsQueryItems)) {
                break;
            }
            realSize = accountDetailsQueryItems.size();
            accountDetailsQueryItemList.addAll(accountDetailsQueryItems);
        } while (realSize == pageSize);
        // 同步开始
        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            return true;
        }

        Date nowDate = new Date();
        String seqNo;
        for (AccountDetailsQueryItem accountDetailsQueryItem : accountDetailsQueryItemList) {   // 同步系统充值
            seqNo = String.format("%s%s%s", accountDetailsQueryItem.getInpDate(), accountDetailsQueryItem.getInpTime(), accountDetailsQueryItem.getTraceNo());
            RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findTopBySeqNo(seqNo);
            if (!ObjectUtils.isEmpty(rechargeDetailLog)) {
                continue;
            }

            Double money = new Double(accountDetailsQueryItem.getTxAmount()) * 100;
            rechargeDetailLog = new RechargeDetailLog();
            rechargeDetailLog.setUserId(userId);
            rechargeDetailLog.setBankName(userThirdAccount.getBankName());
            rechargeDetailLog.setCallbackTime(nowDate);
            rechargeDetailLog.setCreateTime(nowDate);
            rechargeDetailLog.setUpdateTime(nowDate);
            rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
            rechargeDetailLog.setDel(0);
            rechargeDetailLog.setState(1); // 充值成功
            rechargeDetailLog.setMoney(money.longValue());
            rechargeDetailLog.setRechargeChannel(1);  // 其他渠道
            rechargeDetailLog.setRechargeType(1); // 线下充值
            rechargeDetailLog.setSeqNo(seqNo);
            rechargeDetailLogService.save(rechargeDetailLog);

            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.offlineRecharge);  // 招标失败解除冻结资金
            assetChange.setUserId(userId);
            assetChange.setMoney(money.longValue());
            assetChange.setRemark(String.format("成功线下充值%s元", StringHelper.formatDouble(money.longValue() / 100D, true)));
            assetChange.setSourceId(rechargeDetailLog.getId());
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getSeqNo());
            assetChangeProvider.commonAssetChange(assetChange);

            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 30));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doOffLineRechargeByAleve(String date) throws Exception {
        Specification<Aleve> specification = Specifications
                .<Aleve>and()
                .eq("queryDate", date)
                .eq("transtype", "7820")
                .build();

        Long account = aleveService.count(specification);
        if (account == 0) {
            log.info("全网线下同步: aleve查询记录为空");
            return false;
        }

        int pageSize = 1000;
        int pageIndexTotal = account.intValue() / pageSize;
        pageIndexTotal = account % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;
        Date nowDate = new Date();
        for (int pageIndex = 0; pageIndex < pageIndexTotal; pageIndex++) {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            Page<Aleve> lists = aleveService.findAll(specification, pageable);
            if (CollectionUtils.isEmpty(lists.getContent())) {
                break;
            }

            List<Aleve> content = lists.getContent();
            for (Aleve aleve : content) {
                String seqNo = String.format("%s%s%s", aleve.getInpdate(), aleve.getInptime(), aleve.getTranno());
                RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findTopBySeqNo(seqNo);
                if (!ObjectUtils.isEmpty(rechargeDetailLog)) {
                    continue;
                }

                UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(aleve.getCardnbr());
                Preconditions.checkNotNull(userThirdAccount, "AssetSynBizImpl.doOffLineRechargeByAleve: userThirdAccount is empty");
                Double money = new Double(aleve.getAmount()) * 100;
                rechargeDetailLog = new RechargeDetailLog();
                rechargeDetailLog.setUserId(userThirdAccount.getUserId());
                rechargeDetailLog.setBankName(userThirdAccount.getBankName());
                rechargeDetailLog.setCallbackTime(nowDate);
                rechargeDetailLog.setCreateTime(nowDate);
                rechargeDetailLog.setUpdateTime(nowDate);
                rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
                rechargeDetailLog.setDel(0);
                rechargeDetailLog.setState(1); // 充值成功
                rechargeDetailLog.setMoney(money.longValue());
                rechargeDetailLog.setRechargeChannel(1);  // 其他渠道
                rechargeDetailLog.setRechargeType(1); // 线下充值
                rechargeDetailLog.setSeqNo(seqNo);
                rechargeDetailLogService.save(rechargeDetailLog);

                AssetChange assetChange = new AssetChange();
                assetChange.setType(AssetChangeTypeEnum.offlineRecharge);  // 招标失败解除冻结资金
                assetChange.setUserId(userThirdAccount.getUserId());
                assetChange.setMoney(money.longValue());
                assetChange.setRemark(String.format("成功线下充值%s元", StringHelper.formatDouble(money.longValue() / 100D, true)));
                assetChange.setSourceId(rechargeDetailLog.getId());
                assetChange.setSeqNo(assetChangeProvider.getSeqNo());
                assetChange.setGroupSeqNo(assetChangeProvider.getSeqNo());
                assetChangeProvider.commonAssetChange(assetChange);

                // 触发用户充值
                MqConfig mqConfig = new MqConfig();
                mqConfig.setTag(MqTagEnum.RECHARGE);
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
                mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 30));
                ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
                mqConfig.setMsg(body);
                mqHelper.convertAndSend(mqConfig);
            }
        }

        return true;
    }
}
