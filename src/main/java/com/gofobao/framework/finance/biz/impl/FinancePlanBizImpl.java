package com.gofobao.framework.finance.biz.impl;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.finance.vo.request.VoFinancePlanTender;
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
import com.gofobao.framework.tender.vo.response.VoFindAutoTender;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private UserService userService;
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

    /**
     * 理财计划投标
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
        // /更改购买计划状态、资金信息
        return ResponseEntity.ok(VoBaseResp.ok("理财计划投标成功!"));
    }
}
