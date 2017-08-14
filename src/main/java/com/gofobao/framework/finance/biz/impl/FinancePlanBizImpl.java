package com.gofobao.framework.finance.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.finance.vo.request.VoFinancePlanTender;
import com.gofobao.framework.finance.vo.request.VoTenderFinancePlan;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.ThirdAccountHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.response.VoFindAutoTender;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/8/10.
 */
@Service
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

    public static final String MSG = "msg";
    public static final String MONEY = "money";

    /**
     * 理财计划投标
     *
     * @param voTenderFinancePlan
     * @return
     */
    public ResponseEntity<VoBaseResp> tenderFinancePlan(VoTenderFinancePlan voTenderFinancePlan) {
        //前置判断计划可购金额是否充足
        long financePlanId = voTenderFinancePlan.getFinancePlanId();/* 理财计划id */
        long userId = voTenderFinancePlan.getUserId();/* 理财计划购买人id */
        FinancePlan financePlan = financePlanService.findByIdLock(financePlanId); /* 理财计划记录 */
        Preconditions.checkNotNull(financePlan, "理财计划记录不存在!");
        Users users = userService.findById(userId);
        UserThirdAccount buyUserAccount = userThirdAccountService.findByUserId(userId);/* 理财计划购买人id */
        ThirdAccountHelper.allConditionCheck(buyUserAccount);
        //判断购买人资金是否充足
        Asset asset = assetService.findByUserId(userId);/* 购买人账户 */
//        verifyUserInfo4Finance()
        //获取有效的资金

        return ResponseEntity.ok(VoBaseResp.ok("理财计划投标!"));
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
    private boolean verifyUserInfo4Finance(Users user, FinancePlan financePlan, Asset asset, VoTenderFinancePlan voTenderFinancePlan, ImmutableMap<String, Object> immutableMap) {
        String msg = "";
        // 判断用户是否已经锁定
        if (user.getIsLock()) {
            msg = "当前用户属于锁定状态, 如有问题请联系客户!";
            return false;
        }

        // 判断最小投标金额
        long realTenderMoney = financePlan.getMoney() - financePlan.getMoneyYes();  // 剩余金额
        long minLimitTenderMoney = ObjectUtils.isEmpty(financePlan.getLowest()) ? 50 * 100 : financePlan.getLowest();  // 最小投标金额
        long realMiniTenderMoney = Math.min(realTenderMoney, minLimitTenderMoney);  // 获取最小投标金额
        if (realMiniTenderMoney > voTenderFinancePlan.getMoney()) {
            msg = "小于标的最小投标金额!";
            return false;
        }

        // 真实有效投标金额
        long invaildataMoney = Math.min(realTenderMoney, voTenderFinancePlan.getMoney().intValue());
        if (invaildataMoney > asset.getUseMoney()) {
            msg = "您的账户可用余额不足,请先充值!";
            return false;
        }

        // 查询存管系统资金
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            msg = "当前网络不稳定,请稍后重试!";
            return false;
        }

        double availBal = NumberHelper.toDouble(balanceQueryResponse.getAvailBal()) * 100.0;// 可用余额  账面余额-可用余额=冻结金额
        if (availBal < invaildataMoney) {
            msg = "资金账户未同步，请先在个人中心进行资金同步操作!";
            return false;
        }
        immutableMap = ImmutableMap.of(MSG, msg, MONEY, invaildataMoney);
        return true;
    }

    /**
     * 理财计划匹配债权转让
     *
     * @param voFinancePlanTender
     * @return
     */
    public ResponseEntity<VoBaseResp> financePlanTender(VoFinancePlanTender voFinancePlanTender) {
        Date nowDate = new Date();
        //获取paramStr参数、校验参数有效性
        String paramStr = voFinancePlanTender.getParamStr();/* 理财计划投标 */
        if (!SecurityHelper.checkSign(voFinancePlanTender.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "理财计划投标 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        /* 购买债权转让金额 分 */
        long money = (long) NumberHelper.toDouble(paramMap.get("money")) * 100;
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
        FinancePlan financePlan = financePlanService.findById(planId);
        Preconditions.checkNotNull(financePlan, "理财计划记录不存在!");
        /* 债权转让记录*/
        Transfer transfer = transferService.findById(transferId);
        Preconditions.checkNotNull(transfer, "债权转让记录不存在!");
        /* 理财计划购买人存管账户 */
        UserThirdAccount buyUserAccount = userThirdAccountService.findByUserId(userId);
        ThirdAccountHelper.allConditionCheck(buyUserAccount);
        //判断是否是购买人操作
        if (financePlanBuyer.getUserId() != userId) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "操作人非理财计划购买人!"));
        }
        //进行购买债权操作（部分债权转让），并推送到存管系统
        if (transfer.getTransferMoney() == transfer.getTransferMoneyYes()) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "债权转让已全部转出!"));
        }
        /*购买债权有效金额*/
        long validMoney = (long) MathHelper.min(transfer.getTransferMoney() - transfer.getTransferMoneyYes(), money);

        //理财计划购买债权转让
        financePlanBuyTransfer(nowDate, money, transferId, userId, financePlanBuyer, transfer, validMoney);

        /* gfb_asset finance_plan_money 成功后需要扣减 */
        // /更改购买计划状态、资金信息
        return ResponseEntity.ok(VoBaseResp.ok("理财计划投标成功!"));
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
    private void financePlanBuyTransfer(Date nowDate, long money, long transferId, long userId, FinancePlanBuyer financePlanBuyer, Transfer transfer, long validMoney) {
        long alreadyInterest = validMoney / transfer.getTransferMoney() * transfer.getAlreadyInterest();/* 当期应计利息 */
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
        transferBuyLog.setSource(0);
        transferBuyLog.setCreatedAt(nowDate);
        transferBuyLog.setUpdatedAt(nowDate);
        transferBuyLogService.save(transferBuyLog);
        /* 更新债权转让记录 */
        transfer.setUpdatedAt(nowDate);
        transfer.setTransferMoneyYes(transfer.getTransferMoney() + validMoney);
        transferService.save(transfer);
        /* 更新债权转让购买记录 */
        financePlanBuyer.setLeftMoney(financePlanBuyer.getLeftMoney() - validMoney);
        financePlanBuyer.setUpdatedAt(nowDate);
        financePlanBuyerService.save(financePlanBuyer);
    }
}
