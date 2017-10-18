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
import com.gofobao.framework.financial.entity.NewAleve;
import com.gofobao.framework.financial.service.AleveService;
import com.gofobao.framework.financial.service.NewAleveService;
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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;


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

    @Autowired
    RedisHelper redisHelper;

    public static final Gson GSON = new Gson();

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public boolean doAssetSyn(Long userId) throws Exception {
       /* Date nowDate = new Date();
        Users user = userService.findByIdLock(userId);
        Preconditions.checkNotNull(user, "资金同步: 查询用户为空");
        Asset asset = assetService.findByUserIdLock(userId);  // 用户资产
        Preconditions.checkNotNull(asset, "资金同步: 查询用户资产为空");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        ResponseEntity<VoBaseResp> conditionResponse = ThirdAccountHelper.allConditionCheck(userThirdAccount);
        if (!conditionResponse.getStatusCode().equals(HttpStatus.OK)) {
            log.error("当前用户未开户");
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

        double currBal = MoneyHelper.round(MoneyHelper.multiply(NumberHelper.toDouble(balanceQueryResponse.getCurrBal()), 100d), 0);  // 账户总额
        if (new Double(currBal).longValue() <= (asset.getNoUseMoney() + asset.getUseMoney())) {
            log.info("当前用户金额无需同步");
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
            // 排除拨正数据
            accountDetailsQueryItems = accountDetailsQueryItems
                    .stream()
                    .filter(accountDetailsQueryItem ->  !"R".equalsIgnoreCase(accountDetailsQueryItem.getOrFlag()))
                    .collect(Collectors.toList()) ;
            realSize = accountDetailsQueryItems.size();
            accountDetailsQueryItemList.addAll(accountDetailsQueryItems);
        } while (realSize == pageSize);

        // 同步开始
        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            log.info("当前线下用户资金流水为0");
            return true;
        }

        Specification<RechargeDetailLog> rechargeDetailLogSpecification = Specifications
                .<RechargeDetailLog>and()
                .between("createTime", new Range<>(DateHelper.beginOfDate(nowDate), DateHelper.endOfDate(nowDate)))
                .eq("userId", userId)  // 用户ID
                .eq("rechargeChannel", 1) // 线下充值
                .eq("state", 1)  // 充值成功
                .build();

        List<RechargeDetailLog> rechargeDetailLogList = rechargeDetailLogService.findAll(rechargeDetailLogSpecification);

        if (!CollectionUtils.isEmpty(rechargeDetailLogList)) {
            Iterator<RechargeDetailLog> iterator = rechargeDetailLogList.iterator();
            while (iterator.hasNext()) {
                RechargeDetailLog recharge = iterator.next();
                Iterator<AccountDetailsQueryItem> iterator1 = accountDetailsQueryItemList.iterator();
                while (iterator1.hasNext()) {
                    AccountDetailsQueryItem offRecharge = iterator1.next();
                    Double recordRecharge = new Double(MoneyHelper.multiply(offRecharge.getTxAmount(), "100", 0));
                    long money = recordRecharge.longValue();
                    long localMoney = recharge.getMoney().longValue();
                    log.info(String.format("待同步金额: %s , %s", localMoney, money));
                    if (localMoney == money) {
                        log.info(String.format("已成功同步金额: %s , %s", localMoney, money));
                        iterator.remove();
                        iterator1.remove();
                        break;
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            return true;
        }

        String seqNo;
        for (AccountDetailsQueryItem accountDetailsQueryItem : accountDetailsQueryItemList) {
            seqNo = String.format("%s%s%s", accountDetailsQueryItem.getInpDate(), accountDetailsQueryItem.getInpTime(), accountDetailsQueryItem.getTraceNo());
            Double recordRecharge = new Double(MoneyHelper.multiply(accountDetailsQueryItem.getTxAmount(), "100", 0));
            Long money = recordRecharge.longValue();
            RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
            log.info("进入同步环节");
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
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
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
*/
        return true;
    }


    @Autowired
    private NewAleveService newAleveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doOffLineRechargeByAleve(String date) throws Exception {
        String transtype = "7820";  // 线下转账类型
        Long account = aleveService.countOfDateAndTranstype(date, transtype);  // 查询线下充值总数
        if (account == 0) {
            log.info("全网线下同步: aleve查询记录为空");
            return false;
        }

        int pageSize = 1000;
        int pageIndexTotal = account.intValue() / pageSize;
        pageIndexTotal = account % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;
        Date synDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);

        // 时间
        for (int pageIndex = 0; pageIndex < pageIndexTotal; pageIndex++) {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            Page<Aleve> lists = aleveService.findByDateAndTranstype(date, transtype, pageable);
            if (CollectionUtils.isEmpty(lists.getContent())) {
                break;
            }

            List<Aleve> content = lists.getContent();
            for (Aleve aleve : content) {
                String cardnbr = aleve.getCardnbr();
                if (ObjectUtils.isEmpty(cardnbr)) {
                    log.info("全局同步:  账户信息为空");
                    continue;
                }

                UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(cardnbr);
                if (ObjectUtils.isEmpty(userThirdAccount)) {
                    log.info("全局同步:  用户开户信息为空");
                    continue;
                }

                boolean b = doAdminSynAsset(userThirdAccount.getUserId(), synDate);
                if (!b) {
                    log.info("aleve全局同步失败");
                }
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doAdminSynAsset(Long userId, Date synDate) throws Exception {
        Users user = userService.findByIdLock(userId);
        Preconditions.checkNotNull(user, "资金同步: 查询用户为空");
        Asset asset = assetService.findByUserIdLock(userId);  // 用户资产
        Preconditions.checkNotNull(asset, "资金同步: 查询用户资产为空");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        ResponseEntity<VoBaseResp> conditionResponse = ThirdAccountHelper.allConditionCheck(userThirdAccount);
        if (!conditionResponse.getStatusCode().equals(HttpStatus.OK)) {
            log.error("当前用户未开户");
            return false;
        }

        Date nowDate = new Date();
        // 同步时间大于两天
        String transtype = "7820";
        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>();
        if (DateHelper.diffInDays(nowDate, DateHelper.beginOfDate(synDate), false) != 0) {  // 同步大于一天查询数据库
            log.info("进入数据库查询数据同步");
            Specification<NewAleve> specification = Specifications
                    .<NewAleve>and()
                    .eq("cardnbr", userThirdAccount.getAccountId()) // 电子账户
                    .eq("transtype", transtype) // 线下转账类型
                    .eq("queryTime", DateHelper.dateToString(synDate, DateHelper.DATE_FORMAT_YMD_NUM))  // 某一天
                    .ne("revind", 1)  // 不能为拨正数据
                    .build();
            List<NewAleve> aleveLists = newAleveService.findAll(specification);
            if (!ObjectUtils.isEmpty(aleveLists)) {
                for (NewAleve aleve : aleveLists) {
                    AccountDetailsQueryItem item = new AccountDetailsQueryItem();
                    item.setInpDate(aleve.getInpdate());
                    item.setInpTime(aleve.getInptime());
                    item.setTraceNo(aleve.getTranno());
                    item.setTxAmount(aleve.getAmount());
                    accountDetailsQueryItemList.add(item);
                }
            }
        } else {  // 如果一天只能实时查询
            log.info("进入实时数据查询数据同步");
            int pageSize = 20, pageIndex = 1, realSize = 0;
            String accountId = userThirdAccount.getAccountId();  // 存管账户ID
            do {
                AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
                accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
                accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
                accountDetailsQueryRequest.setStartDate(DateHelper.dateToString(synDate, DateHelper.DATE_FORMAT_YMD_NUM));
                accountDetailsQueryRequest.setEndDate(DateHelper.dateToString(synDate, DateHelper.DATE_FORMAT_YMD_NUM));
                accountDetailsQueryRequest.setType("9");
                accountDetailsQueryRequest.setTranType(transtype); //  线下转账
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

                // 排除拨正数据
                accountDetailsQueryItems = accountDetailsQueryItems
                        .stream()
                        .filter(accountDetailsQueryItem -> !"R".equalsIgnoreCase(accountDetailsQueryItem.getOrFlag()))
                        .collect(Collectors.toList());
                realSize = accountDetailsQueryItems.size();
                accountDetailsQueryItemList.addAll(accountDetailsQueryItems);
            } while (realSize == pageSize);
        }


        // 同步开始
        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            log.info("当前线下用户资金流水为0");
            return true;
        }
        Date startDate = DateHelper.endOfDate(DateHelper.subDays(synDate, 1));
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(synDate, 1));
        Specification<RechargeDetailLog> rechargeDetailLogSpecification = Specifications
                .<RechargeDetailLog>and()
                .between("createTime", new Range<>(startDate, endDate))
                .eq("userId", userId)  // 用户ID
                .eq("rechargeChannel", 1) // 线下充值
                .eq("state", 1)  // 充值成功
                .build();

        List<RechargeDetailLog> rechargeDetailLogList = rechargeDetailLogService.findAll(rechargeDetailLogSpecification);
        if (!CollectionUtils.isEmpty(rechargeDetailLogList)) {
            Iterator<RechargeDetailLog> iterator = rechargeDetailLogList.iterator();
            while (iterator.hasNext()) {
                RechargeDetailLog recharge = iterator.next();
                Iterator<AccountDetailsQueryItem> iterator1 = accountDetailsQueryItemList.iterator();
                while (iterator1.hasNext()) {
                    AccountDetailsQueryItem offRecharge = iterator1.next();
                    Double recordRecharge = new Double(MoneyHelper.multiply(offRecharge.getTxAmount(), "100", 0));
                    if (recharge.getMoney() == recordRecharge.longValue()) {
                        log.info("匹配成功");
                        iterator.remove();
                        iterator1.remove();
                        break;
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            log.info("为匹配充值金额为空");
            return true;
        }

        String seqNo;
        Gson gson = new Gson();
        for (AccountDetailsQueryItem accountDetailsQueryItem : accountDetailsQueryItemList) {
            seqNo = String.format("%s%s%s", accountDetailsQueryItem.getInpDate(), accountDetailsQueryItem.getInpTime(), accountDetailsQueryItem.getTraceNo());
            Double recordRecharge = new Double(MoneyHelper.multiply(accountDetailsQueryItem.getTxAmount(), "100", 0));
            Long money = recordRecharge.longValue();
            RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
            log.info(String.format("进入同步环节 %s", gson.toJson(accountDetailsQueryItem)));
            rechargeDetailLog.setUserId(userId);
            rechargeDetailLog.setBankName(userThirdAccount.getBankName());
            rechargeDetailLog.setCallbackTime(synDate);
            rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
            rechargeDetailLog.setDel(0);
            rechargeDetailLog.setState(1); // 充值成功
            rechargeDetailLog.setMoney(money.longValue());
            rechargeDetailLog.setRechargeChannel(1);  // 其他渠道
            rechargeDetailLog.setRechargeType(1); // 线下充值
            rechargeDetailLog.setSeqNo(seqNo);
            rechargeDetailLog.setCreateTime(synDate);
            rechargeDetailLog.setUpdateTime(synDate);
            rechargeDetailLogService.save(rechargeDetailLog);

            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.offlineRecharge);  // 招标失败解除冻结资金
            assetChange.setUserId(userId);
            assetChange.setMoney(money.longValue());
            assetChange.setRemark(String.format("成功线下充值%s元", StringHelper.formatDouble(money.longValue() / 100D, true)));
            assetChange.setSourceId(rechargeDetailLog.getId());
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            assetChangeProvider.commonAssetChange(assetChange);

            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 30));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
        }
        return true;
    }

    @Override
    public void doOfflineSyn(Long userId, String txAmount, String orgSeqNo, String date) throws Exception {
        Users user = userService.findByIdLock(userId);
        Preconditions.checkNotNull(user, "后台触发充值: 查询用户为空");
        Asset asset = assetService.findByUserIdLock(userId);  // 用户资产
        Preconditions.checkNotNull(asset, "后台触发充值: 查询用户资产为空");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        ResponseEntity<VoBaseResp> conditionResponse = ThirdAccountHelper.allConditionCheck(userThirdAccount);
        if (!conditionResponse.getStatusCode().equals(HttpStatus.OK)) {
            log.error("当前用户未开户");
            return;
        }

        // 用户资金记录查询
        int pageSize = 20, pageIndex = 1, realSize = 0;
        String accountId = userThirdAccount.getAccountId();  // 存管账户ID
        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>();
        do {
            AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
            accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
            accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
            accountDetailsQueryRequest.setStartDate(date);
            accountDetailsQueryRequest.setEndDate(date);
            accountDetailsQueryRequest.setType("9");
            accountDetailsQueryRequest.setTranType("7820"); //  线下转账
            accountDetailsQueryRequest.setAccountId(accountId);
            AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                    accountDetailsQueryRequest,
                    AccountDetailsQueryResponse.class);

            if ((ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg();
                log.error(String.format("资金同步失败: %s", msg));
                return;
            }
            pageIndex++;
            Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
            }.getType()));
            List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList());
            if (CollectionUtils.isEmpty(accountDetailsQueryItems)) {
                break;
            }

            // 排除拨正数据
            accountDetailsQueryItems = accountDetailsQueryItems
                    .stream()
                    .filter(accountDetailsQueryItem -> !"R".equalsIgnoreCase(accountDetailsQueryItem.getOrFlag()))
                    .collect(Collectors.toList());
            realSize = accountDetailsQueryItems.size();
            accountDetailsQueryItemList.addAll(accountDetailsQueryItems);
        } while (realSize == pageSize);


        // 同步开始
        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            log.info("当前线下用户资金流水为0");
            return;
        }

        Date synDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);
        Specification<RechargeDetailLog> rechargeDetailLogSpecification = Specifications
                .<RechargeDetailLog>and()
                .between("createTime", new Range<>(DateHelper.beginOfDate(synDate), DateHelper.endOfDate(synDate)))
                .eq("userId", userId)  // 用户ID
                .eq("rechargeChannel", 1)  // 线下充值
                .eq("state", 1)  // 充值成功
                .build();

        List<RechargeDetailLog> rechargeDetailLogList = rechargeDetailLogService.findAll(rechargeDetailLogSpecification);
        if (!CollectionUtils.isEmpty(rechargeDetailLogList)) {
            Iterator<RechargeDetailLog> iterator = rechargeDetailLogList.iterator();
            while (iterator.hasNext()) {
                RechargeDetailLog recharge = iterator.next();
                Iterator<AccountDetailsQueryItem> iterator1 = accountDetailsQueryItemList.iterator();
                while (iterator1.hasNext()) {
                    AccountDetailsQueryItem offRecharge = iterator1.next();
                    Double recordRecharge = new Double(MoneyHelper.multiply(offRecharge.getTxAmount(), "100", 0));
                    long money = recordRecharge.longValue();
                    if (recharge.getMoney() == money) {
                        log.info("线下充值回调匹配成功");
                        iterator.remove();
                        iterator1.remove();
                        break;
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            log.info("为匹配充值金额为空");
            return;
        }

        String seqNo;
        Gson gson = new Gson();
        for (AccountDetailsQueryItem accountDetailsQueryItem : accountDetailsQueryItemList) {
            seqNo = String.format("%s%s%s", accountDetailsQueryItem.getInpDate(), accountDetailsQueryItem.getInpTime(), accountDetailsQueryItem.getTraceNo());

            if (txAmount.equals(accountDetailsQueryItem.getTxAmount())) {
                log.info("线下充值回调覆盖原始seqNo");
                seqNo = orgSeqNo;
            }
            Double recordRecharge = new Double(MoneyHelper.multiply(accountDetailsQueryItem.getTxAmount(), "100", 0));
            Long money = recordRecharge.longValue();
            RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
            log.info(String.format("进入线下充值回调同步环节 %s", gson.toJson(accountDetailsQueryItem)));
            rechargeDetailLog.setUserId(userId);
            rechargeDetailLog.setBankName(userThirdAccount.getBankName());
            rechargeDetailLog.setCallbackTime(synDate);
            rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
            rechargeDetailLog.setDel(0);
            rechargeDetailLog.setState(1); // 充值成功
            rechargeDetailLog.setMoney(money.longValue());
            rechargeDetailLog.setRechargeChannel(1);  // 其他渠道
            rechargeDetailLog.setRechargeType(1); // 线下充值
            rechargeDetailLog.setSeqNo(seqNo);
            rechargeDetailLog.setCreateTime(synDate);
            rechargeDetailLog.setUpdateTime(synDate);
            rechargeDetailLogService.save(rechargeDetailLog);

            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.offlineRecharge);  // 招标失败解除冻结资金
            assetChange.setUserId(userId);
            assetChange.setMoney(money.longValue());
            assetChange.setRemark(String.format("成功线下充值%s元", StringHelper.formatDouble(money.longValue() / 100D, true)));
            assetChange.setSourceId(rechargeDetailLog.getId());
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            assetChangeProvider.commonAssetChange(assetChange);

            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 30));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
        }
    }
}
