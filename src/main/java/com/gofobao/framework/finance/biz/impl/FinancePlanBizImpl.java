package com.gofobao.framework.finance.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeReq;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeResp;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeReq;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeResp;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.constants.FinannceContants;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.finance.vo.request.*;
import com.gofobao.framework.finance.vo.response.*;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.listener.providers.FinancePlanProvider;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by Zeke on 2017/8/10.
 */
@Service
@Slf4j
public class FinancePlanBizImpl implements FinancePlanBiz {

    final Gson GSON = new GsonBuilder().create();
    @Autowired
    private AssetService assetService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private FinancePlanBuyerService financePlanBuyerService;
    @Autowired
    private FinancePlanService financePlanService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private TransferBuyLogService transferBuyLogService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private UserService userService;
    @Autowired
    private AssetChangeProvider assetChangeProvider;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private FinancePlanProvider financePlanProvider;
    @Value("${gofobao.adminDomain}")
    private String adminDomain;


    //过滤掉 状态; 1:发标待审 ；2：初审不通过；4：复审不通过；5：已取消
    private static List<Integer> statusArray = Lists.newArrayList(FinannceContants.CANCEL,
            FinannceContants.CHECKED_NO_PASS,
            FinannceContants.NO_PASS,
            FinannceContants.PENDINGTRIAL);


    /**
     * 理财计划回购
     *
     * @param voFinanceRepurchase
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoBaseResp> financeRepurchase(VoFinanceRepurchase voFinanceRepurchase) throws Exception {
        Date nowDate = new Date();
        //获取paramStr参数、校验参数有效性
        String paramStr = voFinanceRepurchase.getParamStr();/* 理财计划投标 */
        if (!SecurityHelper.checkSign(voFinanceRepurchase.getSign(), paramStr)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "理财计划回购 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        /* 理财计划回购债权转让id */
        long transferId = NumberHelper.toLong(paramMap.get("transferId"));
        /* 理财计划回购债权转让记录 */
        Transfer transfer = transferService.findByIdLock(transferId);
        //默认zfh为回购人
        long repurchaseUserId = 22002;
        //回购金额
        long principal = transfer.getPrincipal();
        /* 债权购买记录 */
        TransferBuyLog transferBuyLog = new TransferBuyLog();
        transferBuyLog.setTransferId(transferId);
        transferBuyLog.setState(0);
        transferBuyLog.setAlreadyInterest(0l);
        transferBuyLog.setBuyMoney(principal);
        transferBuyLog.setValidMoney(principal);
        transferBuyLog.setPrincipal(principal);
        transferBuyLog.setUserId(repurchaseUserId);
        transferBuyLog.setAuto(false);
        transferBuyLog.setAutoOrder(0);
        transferBuyLog.setDel(false);
        transferBuyLog.setSource(0);
        transferBuyLog.setType(0);
        transferBuyLog.setCreatedAt(nowDate);
        transferBuyLog.setUpdatedAt(nowDate);
        transferBuyLogService.save(transferBuyLog);

        //理财计划回购债权转让
        if (transfer.getTransferMoneyYes() >= transfer.getTransferMoney()) {
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(transferId),
                            MqConfig.IS_REPURCHASE, "true",
                            MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            Boolean result = financePlanProvider.againVerifyFinanceTransfer(body);
            if (!result) {
                throw new Exception("回购失败!");
            }
        }
        return ResponseEntity.ok(VoBaseResp.ok("回购成功!"));
    }

    /**
     * 理财计划资金变动
     *
     * @param voFinancePlanAssetChange
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> financePlanAssetChange(VoFinancePlanAssetChange voFinancePlanAssetChange) throws Exception {
        String paramStr = voFinancePlanAssetChange.getParamStr();
        if (!SecurityHelper.checkSign(voFinancePlanAssetChange.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "债权转让初审 签名验证不通过!"));
        }

        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        long money = new Double(MoneyHelper.round(NumberHelper.toDouble(paramMap.get("money")) * 100, 0)).longValue();/* 资金变动金额 */
        long userId = NumberHelper.toLong(paramMap.get("userId"));/* 资金变动会员id */
        long planBuyId = NumberHelper.toLong(paramMap.get("planBuyId"));/* 理财计划购买id */
        String type = StringHelper.toString(paramMap.get("type"));/* 资金变动类型 */
        String remark = paramMap.get("remark");/* 备注 */
        boolean freezeFlag = BooleanUtils.toBoolean(paramMap.get("freezeFlag"));/* 是否冻结标识 */
        boolean sendRedPacKetFlag = BooleanUtils.toBoolean(paramMap.get("sendRedPacKetFlag"));/* 是否冻结标识 */
        /* 理财计划购买记录 */
        FinancePlanBuyer financePlanBuyer = financePlanBuyerService.findByIdLock(planBuyId);
        Preconditions.checkNotNull(financePlanBuyer, "理财计划购买记录不存在!");
        /* 理财计划购买人 */
        UserThirdAccount buyUserThirdAccount = userThirdAccountService.findByUserId(userId);
        ThirdAccountHelper.allConditionCheck(buyUserThirdAccount);
        /* 资金变动类型 */
        AssetChangeTypeEnum typeEnum = AssetChangeTypeEnum.findType(type);
        Preconditions.checkNotNull(typeEnum, "资金变动类型不存在!");
        /* 购买理财计划解冻id */
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        try {
            //理财计划资金变动
            AssetChange assetChange = new AssetChange();
            assetChange.setForUserId(userId);
            assetChange.setUserId(financePlanBuyer.getUserId());
            assetChange.setType(typeEnum);
            assetChange.setRemark(remark);
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setMoney(money);
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            assetChange.setSourceId(financePlanBuyer.getId());
            assetChangeProvider.commonAssetChange(assetChange);
            //冻结存管金额
            if (freezeFlag) {
                //冻结购买债权转让人资金账户
                BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
                balanceFreezeReq.setAccountId(buyUserThirdAccount.getAccountId());
                balanceFreezeReq.setTxAmount(StringHelper.formatDouble(money, 100, false));
                balanceFreezeReq.setOrderId(orderId);
                balanceFreezeReq.setChannel(ChannelContant.HTML);
                BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
                if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
                    throw new Exception("理财计划资金变动冻结资金失败：" + balanceFreezeResp.getRetMsg());
                }
            }
            //是否发红包
            if (sendRedPacKetFlag) {
                long redpackAccountId = assetChangeProvider.getRedpackAccountId();

                UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redpackAccountId);
                VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                voucherPayRequest.setAccountId(redpackAccount.getAccountId());
                voucherPayRequest.setTxAmount(StringHelper.formatDouble(money, 100, false));
                voucherPayRequest.setForAccountId(buyUserThirdAccount.getAccountId());
                voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                voucherPayRequest.setDesLine(typeEnum.getOpName());
                voucherPayRequest.setChannel(ChannelContant.HTML);
                VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                    String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();

                    log.error("redPacket" + msg);
                    throw new Exception(msg);
                }
            }
        } catch (Throwable t) {
            String newOrderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);/* 购买债权转让冻结金额 orderid */
            //解除存管资金冻结
            BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
            balanceUnfreezeReq.setAccountId(buyUserThirdAccount.getAccountId());
            balanceUnfreezeReq.setTxAmount(StringHelper.formatDouble(money, 100.0, false));
            balanceUnfreezeReq.setChannel(ChannelContant.HTML);
            balanceUnfreezeReq.setOrderId(newOrderId);
            balanceUnfreezeReq.setOrgOrderId(orderId);
            BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
            if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
                throw new Exception("理财计划资金变动解冻资金失败：" + balanceUnfreezeResp.getRetMsg());
            }
            throw new Exception(t);
        }

        return ResponseEntity.ok(VoBaseResp.ok("理财计划资金变动!"));
    }

    /**
     * 理财计划投标
     *
     * @param voTenderFinancePlan
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> tenderFinancePlan(VoTenderFinancePlan voTenderFinancePlan) throws Exception {
        voTenderFinancePlan.setMoney(voTenderFinancePlan.getMoney() * 100);
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);/* 购买理财计划解冻id */
        //前置判断计划可购金额是否充足
        long financePlanId = voTenderFinancePlan.getFinancePlanId();/* 理财计划id */
        long userId = voTenderFinancePlan.getUserId();/* 理财计划购买人id */
        FinancePlan financePlan = financePlanService.findByIdLock(financePlanId); /* 理财计划记录 */
        Preconditions.checkNotNull(financePlan, "理财计划记录不存在!");
        Users users = userService.findById(userId);
        UserThirdAccount buyUserAccount = userThirdAccountService.findByUserId(userId);/* 理财计划购买人id */
        ThirdAccountHelper.allConditionCheck(buyUserAccount);
        Asset asset = assetService.findByUserIdLock(userId);/* 购买人账户 */
        Preconditions.checkNotNull(asset, "购买人账户记录不存在!");
        //验证理财用户账户
        Multiset<String> errerMessage = HashMultiset.create();
        boolean flag = verifyUserByFinancePlan(users, financePlan, asset, voTenderFinancePlan, errerMessage);
        Set<String> errorSet = errerMessage.elementSet();
        Iterator<String> iterator = errorSet.iterator();
        if (!flag) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, iterator.next(), VoBaseResp.class/*VoViewFinancePlanTender.class)*/));
        }
        /* 购买理财计划有效金额 */
        long validateMoney = NumberHelper.toLong(iterator.next());
        //验证理财计划
        errerMessage = HashMultiset.create();
        flag = verifyFinancePlan(errerMessage, financePlanId, userId, financePlan);
        errorSet = errerMessage.elementSet();
        iterator = errorSet.iterator();
        if (!flag) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, iterator.next(), VoBaseResp.class));
        }
        //生成理财计划购买记录
        FinancePlanBuyer financePlanBuyer = addFinancePlanBuyer(voTenderFinancePlan, userId, financePlan, validateMoney);
        try {
            //理财计划更新
            updateFinancePlan(financePlan, validateMoney);
        } catch (Exception e) {
            throw new Exception("系统异常,请稍后再试");
        }
        //更新理财计划购买人的资金账户
        try {
            updateAssetByBuyFinancePlan(financePlan, buyUserAccount, validateMoney, financePlanBuyer, orderId);
        } catch (Exception e) {
            String newOrderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);/* 购买债权转让冻结金额 orderid */
            //解除存管资金冻结
            BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
            balanceUnfreezeReq.setAccountId(buyUserAccount.getAccountId());
            balanceUnfreezeReq.setTxAmount(StringHelper.formatDouble(validateMoney, 100.0, false));
            balanceUnfreezeReq.setChannel(ChannelContant.HTML);
            balanceUnfreezeReq.setOrderId(newOrderId);
            balanceUnfreezeReq.setOrgOrderId(orderId);
            BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
            if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
                throw new Exception("理财计划投标解冻资金失败：" + balanceUnfreezeResp.getRetMsg());
            }
        }
        //是否满额 满额通知后台
        if (financePlan.getMoney().intValue() == financePlan.getMoneyYes().intValue()) {
            log.info(String.format("当前理财已满标,通知后台：planId%s", financePlan.getId()));
            Map<String, String> bodyMap = ImmutableMap.of("planId", financePlan.getId().toString());
            try {
                Map<String, String> requestMaps = ImmutableMap.of("paramStr", GSON.toJson(bodyMap), "sign", SecurityHelper.getSign(GSON.toJson(bodyMap)));
                String resultStr = OKHttpHelper.postForm(adminDomain + "/api/open/finance-plan/review", requestMaps, null);
                System.out.print("返回响应:" + resultStr);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return ResponseEntity.ok(VoBaseResp.ok("购买成功", VoBaseResp.class));
    }

    private FinancePlan updateFinancePlan(FinancePlan financePlan, long validMoney) {
        financePlan.setMoneyYes(financePlan.getMoneyYes() + validMoney);
        financePlan.setLeftMoney(financePlan.getLeftMoney() + validMoney);
        financePlan.setRightMoney(0L);
        financePlan.setTotalSubPoint(financePlan.getTotalSubPoint() + 1);
        financePlan.setUpdatedAt(new Date());
        return financePlanService.save(financePlan);
    }

    /**
     * 生成理财计划购买记录
     *
     * @param voTenderFinancePlan
     * @param userId
     * @param financePlan
     * @param validateMoney
     * @return
     */
    private FinancePlanBuyer addFinancePlanBuyer(VoTenderFinancePlan voTenderFinancePlan, long userId, FinancePlan financePlan, long validateMoney) {
        int requestSource = 0;
        try {
            requestSource = Integer.valueOf(voTenderFinancePlan.getRequestSource());
        } catch (Exception e) {
            requestSource = 0;
        }

        FinancePlanBuyer financePlanBuyer = new FinancePlanBuyer();
        financePlanBuyer.setUserId(userId);
        financePlanBuyer.setMoney(new Double(voTenderFinancePlan.getMoney()).longValue());
        financePlanBuyer.setValidMoney(validateMoney); //有效金额
        financePlanBuyer.setLeftMoney(+validateMoney);  //剩余未匹配金额(池子里面的钱)
        financePlanBuyer.setRightMoney(0L);//已匹配金额
        financePlanBuyer.setUpdatedAt(new Date());
        financePlanBuyer.setApr(financePlan.getBaseApr());
        financePlanBuyer.setRemark(voTenderFinancePlan.getRemark());
        financePlanBuyer.setCreatedAt(new Date());
        financePlanBuyer.setApr(financePlan.getBaseApr());
        financePlanBuyer.setEndLockAt(financePlan.getEndLockAt());
        financePlanBuyer.setStatus(1);
        financePlanBuyer.setSource(requestSource);
        financePlanBuyer.setPlanId(financePlan.getId());
        financePlanBuyer.setUpdatedAt(new Date());
        financePlanBuyer.setState(1);
        financePlanBuyerService.save(financePlanBuyer);
        return financePlanBuyer;
    }

    /**
     * 更新理财计划购买人的资金账户
     *
     * @param financePlan
     * @param buyUserAccount
     * @param validateMoney
     * @param financePlanBuyer
     * @throws Exception
     */
    private void updateAssetByBuyFinancePlan(FinancePlan financePlan, UserThirdAccount buyUserAccount, long validateMoney, FinancePlanBuyer financePlanBuyer, String orderId) throws Exception {
        //冻结资金，将购买资金划到计划冻结 不要冻结
        AssetChange assetChange = new AssetChange();
        assetChange.setForUserId(financePlanBuyer.getUserId());
        assetChange.setUserId(financePlanBuyer.getUserId());
        assetChange.setType(AssetChangeTypeEnum.financePlanFreeze);
        assetChange.setRemark(String.format("成功购买理财计划[%s]冻结资金%s元", financePlan.getName(), StringHelper.formatDouble(validateMoney / 100D, true)));
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setMoney(validateMoney);
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChange.setSourceId(financePlanBuyer.getId());
        assetChangeProvider.commonAssetChange(assetChange);
        //冻结购买债权转让人资金账户
        /*BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(buyUserAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(validateMoney, 100, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
            throw new Exception("即信批次还款冻结资金失败：" + balanceFreezeResp.getRetMsg());
        }*/
        //保存存管冻结orderId
/*        financePlanBuyer.setFreezeOrderId(orderId);*/
        financePlanBuyerService.save(financePlanBuyer);
    }

    /**
     * 验证理财计划
     *
     * @param errerMessage
     * @param financePlanId
     * @param userId
     * @param financePlan
     * @return
     */
    private boolean verifyFinancePlan(Multiset<String> errerMessage, long financePlanId, long userId, FinancePlan financePlan) {
        //验证理财计划
        int status = financePlan.getStatus();/* 理财计划状态 */
        //判断是否满额
        if (financePlan.getMoneyYes() >= financePlan.getMoney()) {
            errerMessage.add("理财计划已满额，暂无可投金额！");
            return false;
        }
        if (status != 1) {
            errerMessage.add("理财计划不可购买!");
            return true;
        }
        //判断理财计划是否结束
        if (financePlan.getFinishedState()) {
            errerMessage.add("理财计划已结束!");
            return true;
        }
        //判断是否频繁购买
        boolean bool = financePlanBuyerService.checkFinancePlanBuyNimiety(financePlanId, userId);
        if (bool) {
            errerMessage.add("理财计划购买频繁!");
            return true;
        }
        return false;
    }

    /**
     * 投资理财计划用户审核检查
     * <p>
     * 主要做一下教研:
     * 1. 用户是否锁定
     * 2.投标是否满足最小投标原则
     * 3.有效金额是否大于自动投标设定的最大投标金额
     * 4.存管金额匹配
     * 4.账户有效金额匹配
     *
     * @param user
     * @param financePlan
     * @param asset
     * @param voTenderFinancePlan
     * @return
     */
    private boolean verifyUserByFinancePlan(Users user, FinancePlan financePlan, Asset asset, VoTenderFinancePlan voTenderFinancePlan, Multiset<String> errerMessage) {
        // 判断用户是否已经锁定
        if (user.getIsLock()) {
            errerMessage.add("当前用户属于锁定状态, 如有问题请联系客户!");
            return false;
        }
        if (asset.getUseMoney().longValue() < voTenderFinancePlan.getMoney().longValue()) {
            errerMessage.add("对不起余额不足!");
            return false;
        }
        // 判断最小投标金额
        long realTenderMoney = financePlan.getMoney() - financePlan.getMoneyYes();  // 剩余金额
        Long money = voTenderFinancePlan.getMoney().longValue();
        if (money < realTenderMoney) { //如果不能满标
            long minLimitTenderMoney = ObjectUtils.isEmpty(financePlan.getLowest()) ? 50 * 100 : financePlan.getLowest();  // 最小投标金额
            long realMiniTenderMoney = Math.min(realTenderMoney, minLimitTenderMoney);  // 获取最小投标金额
            if (realMiniTenderMoney > voTenderFinancePlan.getMoney()) { //是否小于最小投标金额
                errerMessage.add("对不起！不能小于投标金额");
                return false;
            } else if ((financePlan.getAppendMultipleAmount() > 0) && (money % financePlan.getAppendMultipleAmount() != 0)) {  //投标金额不为倍数
                errerMessage.add("对不起！购买金额"
                        + StringHelper.formatDouble(financePlan.getAppendMultipleAmount(), 100, false)
                        + "的整倍数");
                return false;
            }
        }
        // 真实有效投标金额
        long invaildataMoney = Math.min(realTenderMoney, voTenderFinancePlan.getMoney().intValue());
        if (invaildataMoney > asset.getUseMoney()) {
            errerMessage.add("您的账户可用余额不足,请先充值!");
            return false;
        }

        // 查询存管系统资金
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            errerMessage.add("当前网络不稳定,请稍后重试!");
            return false;
        }

        long availBal = MoneyHelper.yuanToFen(NumberHelper.toDouble(balanceQueryResponse.getAvailBal()));
        long useMoney = asset.getUseMoney().longValue();
        if (availBal < useMoney) {
            log.error(String.format("资金账户未同步:本地:%s 即信:%s", useMoney, availBal));
            errerMessage.add("资金账户未同步，请先在个人中心进行资金同步操作!");
            return false;
        }
        errerMessage.add(String.valueOf(invaildataMoney));
        return true;
    }


    /**
     * 理财计划匹配债权转让
     *
     * @param voFinancePlanTender
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> financePlanTender(VoFinancePlanTender voFinancePlanTender) throws Exception {

        Date nowDate = new Date();
        //获取paramStr参数、校验参数有效性
        String paramStr = voFinancePlanTender.getParamStr();/* 理财计划投标 */
        if (!SecurityHelper.checkSign(voFinancePlanTender.getSign(), paramStr)) {
            return ResponseEntity.badRequest().body("理财计划投标 签名验证不通过!");
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        /* 购买债权转让金额 分 */
        long money = new Double(MoneyHelper.round(MoneyHelper.multiply(NumberHelper.toDouble(paramMap.get("money")), 100d), 0)).longValue();
        /* 债权转让id */
        long transferId = NumberHelper.toLong(paramMap.get("transferId"));
        /* 购买理财计划id */
        long buyerId = NumberHelper.toLong(paramMap.get("buyerId"));
        /* 理财计划id */
        long planId = NumberHelper.toLong(paramMap.get("planId"));
        /* 购买计划人id */
        long userId = NumberHelper.toLong(paramMap.get("userId"));
        /* 理财计划购买记录 */
        FinancePlanBuyer financePlanBuyer = financePlanBuyerService.findByIdLock(buyerId);
        Preconditions.checkNotNull(financePlanBuyer, "理财计划购买记录不存在!");
        /* 理财计划记录 */
        FinancePlan financePlan = financePlanService.findByIdLock(planId);
        Preconditions.checkNotNull(financePlan, "理财计划记录不存在!");
        /* 债权转让记录*/
        Transfer transfer = transferService.findById(transferId);
        Preconditions.checkNotNull(transfer, "债权转让记录不存在!");
        /* 理财计划购买人存管账户 */
        UserThirdAccount buyUserAccount = userThirdAccountService.findByUserId(userId);
        ThirdAccountHelper.allConditionCheck(buyUserAccount);
        //判断是否是购买人操作
        if (financePlanBuyer.getUserId() != userId) {
            throw new Exception("匹配失败!");
        }
        //进行购买债权操作（部分债权转让），并推送到存管系统
        if (transfer.getTransferMoney() == transfer.getTransferMoneyYes()) {
            throw new Exception("匹配失败!");
        }
        /*购买债权有效金额*/
        long validMoney = (long) MathHelper.min(transfer.getTransferMoney() - transfer.getTransferMoneyYes(), money);
        if (validMoney <= 0) {
            throw new Exception("匹配失败!");
        }
        //理财计划购买债权转让
        TransferBuyLog transferBuyLog = financePlanBuyTransfer(nowDate, money, transferId, userId, financePlanBuyer, financePlan, transfer, validMoney);
        if (transfer.getTransferMoneyYes() >= transfer.getTransferMoney()) {
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(transferId),
                            MqConfig.IS_REPURCHASE, "false",
                            MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            Boolean result = financePlanProvider.againVerifyFinanceTransfer(body);
            if (!result) {
                throw new Exception("匹配失败!");
            }
        }
        return ResponseEntity.ok(GSON.toJson(transferBuyLog));

    }

    /**
     * 理财计划复审
     *
     * @param voFinanceAgainVerifyTransfer
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> financeAgainVerifyTransfer(VoFinanceAgainVerifyTransfer voFinanceAgainVerifyTransfer) {
        String paramStr = voFinanceAgainVerifyTransfer.getParamStr();
        if (!SecurityHelper.checkSign(voFinanceAgainVerifyTransfer.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc理财计划复审 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long transferId = NumberHelper.toLong(paramMap.get("transferId"));
        /* 理财计划债权转让记录 */
        Transfer transfer = transferService.findByIdLock(transferId);
        if (transfer.getTransferMoneyYes() > 0 && transfer.getType().intValue() == 1 && transfer.getState().intValue() == 1) {
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_FINANCE_PLAN);
            mqConfig.setTag(MqTagEnum.AGAIN_VERIFY_FINANCE_TRANSFER);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(transferId),
                            MqConfig.IS_REPURCHASE, "false",
                            MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            log.info(String.format("FinancePlanBizImpl financeAgainVerifyTransfer send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
            return ResponseEntity.ok(VoBaseResp.ok("复审成功!"));
        }
        return ResponseEntity.ok(VoBaseResp.ok("复审失败!"));
    }

    /**
     * 理财计划购买债权转让
     *
     * @param nowDate
     * @param money
     * @param transferId
     * @param userId
     * @param financePlanBuyer
     * @param transfer
     * @param validMoney
     */
    private TransferBuyLog financePlanBuyTransfer(Date nowDate,
                                                  long money,
                                                  long transferId,
                                                  long userId,
                                                  FinancePlanBuyer financePlanBuyer,
                                                  FinancePlan financePlan,
                                                  Transfer transfer,
                                                  long validMoney) {
        double transferFeeRadio = MoneyHelper.divide(validMoney, transfer.getTransferMoney());
        long alreadyInterest = new Double(MoneyHelper.multiply(transferFeeRadio, transfer.getAlreadyInterest())).longValue();/* 当期应计利息 */
        long principal = validMoney - alreadyInterest;/* 债权份额 */
        /* 债权购买记录 */
        TransferBuyLog transferBuyLog = new TransferBuyLog();
        transferBuyLog.setTransferId(transferId);
        transferBuyLog.setState(0);
        transferBuyLog.setAlreadyInterest(alreadyInterest);
        transferBuyLog.setBuyMoney(money);
        transferBuyLog.setValidMoney(validMoney);
        transferBuyLog.setPrincipal(principal);
        transferBuyLog.setUserId(userId);
        transferBuyLog.setAuto(false);
        transferBuyLog.setAutoOrder(0);
        transferBuyLog.setDel(false);
        transferBuyLog.setFinanceBuyId(financePlanBuyer.getId());
        transferBuyLog.setSource(0);
        transferBuyLog.setType(1);
        transferBuyLog.setCreatedAt(nowDate);
        transferBuyLog.setUpdatedAt(nowDate);
        transferBuyLogService.save(transferBuyLog);
        /* 更新债权转让记录 */
        transfer.setUpdatedAt(nowDate);
        transfer.setTransferMoneyYes(transfer.getTransferMoneyYes() + validMoney);
        transferService.save(transfer);
        /* 更新理财计划购买记录 */
        financePlanBuyer.setLeftMoney(financePlanBuyer.getLeftMoney() - validMoney);
        financePlanBuyer.setRightMoney(financePlanBuyer.getRightMoney() + validMoney);
        financePlanBuyer.setUpdatedAt(nowDate);
        financePlanBuyerService.save(financePlanBuyer);
        /* 更新理财计划记录 */
        financePlan.setLeftMoney(financePlan.getLeftMoney() - validMoney);
        financePlan.setRightMoney(financePlan.getRightMoney() + validMoney);

        return transferBuyLog;
    }


    /**
     * 理财列表
     *
     * @param page
     * @return
     */
    @Override
    public ResponseEntity<PlanListWarpRes> list(Page page) {

        PlanListWarpRes warpRes = VoBaseResp.ok("查询成功", PlanListWarpRes.class);

        Specification<FinancePlan> specification = Specifications.<FinancePlan>and()
                .notIn("status", statusArray.toArray())
                .eq("type", 0)
                .build();
        List<FinancePlan> financePlans = financePlanService.findList(specification, new Sort(Sort.Direction.DESC, "id"));

        if (CollectionUtils.isEmpty(financePlans)) {
            return ResponseEntity.ok(warpRes);
        }
        List<PlanList> planLists = new ArrayList<>();
        //装配结果集
        financePlans.stream().forEach(p -> {
            PlanList plan = new PlanList();
            plan.setApr(StringHelper.formatMon(p.getBaseApr() / 100D));
            plan.setId(p.getId());
            plan.setMoney(StringHelper.formatMon(p.getMoney() / 100D));
            plan.setSpend(MoneyHelper.round((p.getMoneyYes() / p.getMoney().doubleValue()) * 100, 2));
            plan.setTimeLimit(p.getTimeLimit());
            plan.setStatus(handleStatus(p));
            plan.setPlanName(p.getName());
            planLists.add(plan);
        });
        warpRes.setPlanLists(planLists);
        return ResponseEntity.ok(warpRes);
    }

    /**
     * 理财列表
     *
     * @param page
     * @return
     */
    @Override
    public ResponseEntity<VoViewFinanceServerPlanResp> financeServerlist(Page page) {

        VoViewFinanceServerPlanResp warpRes = VoBaseResp.ok("查询成功", VoViewFinanceServerPlanResp.class);

        Specification<FinancePlan> specification = Specifications.<FinancePlan>and()
                .notIn("status", statusArray.toArray())
                .eq("type", 1)
                .build();
        List<FinancePlan> financePlans = financePlanService.findList(specification, new Sort(Sort.Direction.DESC, "id"));

        if (CollectionUtils.isEmpty(financePlans)) {
            return ResponseEntity.ok(warpRes);
        }
        List<FinanceServerPlan> planLists = new ArrayList<>();
        //装配结果集
        financePlans.stream().forEach(p -> {
            FinanceServerPlan financeServerPlan = new FinanceServerPlan();
            financeServerPlan.setApr(StringHelper.formatMon(p.getBaseApr() / 100D));
            financeServerPlan.setId(p.getId());
            financeServerPlan.setMoney(StringHelper.formatMon(p.getMoney() / 100D));
            financeServerPlan.setSpend(MoneyHelper.round((p.getMoneyYes() / p.getMoney().doubleValue()) * 100, 2));
            financeServerPlan.setTimeLimit(p.getTimeLimit());
            financeServerPlan.setStatus(handleStatus(p));
            financeServerPlan.setPlanName(p.getName());
            planLists.add(financeServerPlan);
        });
        warpRes.setFinanceServerPlanList(planLists);
        return ResponseEntity.ok(warpRes);
    }


    /**
     * 理财详情
     *
     * @param id
     * @return
     */
    @Override
    public ResponseEntity<PlanDetail> details(Long id) {
        FinancePlan financePlan = financePlanService.findById(id);
        if (ObjectUtils.isEmpty(financePlan) || statusArray.contains(financePlan.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(PlanDetail.error(
                            VoBaseResp.ERROR,
                            "非法访问",
                            PlanDetail.class));
        }

        PlanDetail planDetail = VoBaseResp.ok("查询成功", PlanDetail.class);
        //年利率
        Integer apr = financePlan.getBaseApr();
        planDetail.setApr(StringHelper.formatMon(apr / 100D));
        //购买次数
        planDetail.setBuyCount(financePlan.getTotalSubPoint());
        //计划金额
        Long money = financePlan.getMoney();
        planDetail.setMoney(StringHelper.formatMon(financePlan.getMoney() / 100D));
        //id
        planDetail.setId(financePlan.getId());
        //名称
        planDetail.setName(financePlan.getName());
        //起投金额
        planDetail.setLowMoney(StringHelper.formatMon(financePlan.getLowestShow() / 100D));
        planDetail.setHideLowMoney(financePlan.getLowest() / 100D);
        //已投金额
        Long moneyYes = financePlan.getMoneyYes();
        //剩余金额
        planDetail.setShowSurplusMoney(StringHelper.formatMon((money - moneyYes) / 100D));
        planDetail.setSurplusMoney(money - moneyYes);

        planDetail.setSpend(MoneyHelper.round((moneyYes / money.doubleValue()) * 100, 1));
        Integer timeLimit = financePlan.getTimeLimit();

        //预期收益
        BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(100 * 10000), new Double(apr), timeLimit, financePlan.getCreatedAt());
        Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(2);
        Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));
        planDetail.setEarnings(StringHelper.formatMon(earnings / 100D));
        //状态、
        Integer status = handleStatus(financePlan);
        //剩余时间
        Date endDate = DateHelper.endOfDate(financePlan.getCreatedAt());
        Date nowDate = new Date();
        if (status.intValue() == FinannceContants.PURCJASE) {
            planDetail.setSurplusSecond(endDate.getTime() - nowDate.getTime());
        }

        planDetail.setTimeLimit(financePlan.getTimeLimit());
        //截至日期
        planDetail.setEndAt(DateHelper.dateToString(endDate));
        planDetail.setEndLockAt(!StringUtils.isEmpty(financePlan.getEndLockAt())
                ? DateHelper.dateToString(financePlan.getEndLockAt(), DateHelper.DATE_FORMAT_YMD)
                : "");
        //起始日期
        planDetail.setStartAt(DateHelper.dateToString(financePlan.getCreatedAt()));
        planDetail.setStatus(status);
        return ResponseEntity.ok(planDetail);
    }

    /**
     * 状态处理
     *
     * @param financePlan
     * @return
     */
    private Integer handleStatus(FinancePlan financePlan) {
        Integer status = financePlan.getStatus();
        //购买中
        if (status.intValue() == FinannceContants.PURCJASE) {
            //未满标
            if (!StringUtils.isEmpty(financePlan.getSuccessAt())) {
                return 2;   //售罄
            }
            return status;
            //满标
        } else if (status.intValue() == FinannceContants.END) {
            //已结清
            if (!financePlan.getFinishedState()) {
                return 3;//还款中
            } else {
                return 4; //已完成
            }
        }
        return status;
    }

    /**
     * 计划合同
     *
     * @param planId
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> flanContract(Long planId, Long userId) {
        FinancePlan financePlan = financePlanService.findById(planId);
        PlanContract planContract = new PlanContract();
        planContract.setTimeLimit(financePlan.getTimeLimit() + BorrowContants.MONTH);
        planContract.setMoney(StringHelper.formatMon(financePlan.getMoney() / 100d));
        planContract.setRepayFashion("按月分期");
        planContract.setPlanName(financePlan.getName());
        Users users = null;
        if (userId.intValue() > 0) {
            users = userService.findById(userId);
        }
        if (!ObjectUtils.isEmpty(users)) {
            Specification<FinancePlanBuyer> planBuyerSpecification = Specifications.<FinancePlanBuyer>and()
                    .eq("userId", userId)
                    .eq("planId", planId)
                    .eq("status", 1)
                    .build();
            List<FinancePlanBuyer> financePlanBuyers = financePlanBuyerService.findList(planBuyerSpecification);
            if (!CollectionUtils.isEmpty(financePlanBuyers)) {
                FinancePlanBuyer financePlanBuyer = financePlanBuyers.get(0);
                planContract.setApr(StringHelper.formatMon(financePlan.getBaseApr()));
                planContract.setCreatedAt(DateHelper.dateToString(financePlanBuyer.getCreatedAt()));
                planContract.setTenderMoney(StringHelper.formatMon(financePlanBuyer.getValidMoney() / 100D));
                planContract.setIdCard(users.getCardId());
            }
        }
        return GSON.fromJson(GSON.toJson(planContract), new TypeToken<Map<String, Object>>() {
        }.getType());
    }
}
