package com.gofobao.framework.finance.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.constants.FinannceContants;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.finance.vo.request.VoFinancePlanTender;
import com.gofobao.framework.finance.vo.response.PlanBuyUserListWarpRes;
import com.gofobao.framework.finance.vo.response.PlanDetail;
import com.gofobao.framework.finance.vo.response.PlanList;
import com.gofobao.framework.finance.vo.response.PlanListWarpRes;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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


    /**
     * 理财列表
     *
     * @param page
     * @return
     */
    @Override
    public ResponseEntity<PlanListWarpRes> list(Page page) {

        PlanListWarpRes warpRes = VoBaseResp.ok("查询成功", PlanListWarpRes.class);

        //过滤掉 状态; 1:发标待审 ；2：初审不通过；4：复审不通过；5：已取消
        List<Integer> statusArray = Lists.newArrayList(FinannceContants.CANCEL,
                FinannceContants.CHECKED_NO_PASS,
                FinannceContants.NO_PASS,
                FinannceContants.PURCJASE);

        Specification<FinancePlan> specification = Specifications.<FinancePlan>and()
                .ne("status", statusArray.toArray())
                .build();
        List<FinancePlan> financePlans = financePlanService.findList(specification, new Sort(Sort.Direction.DESC, "id"));

        if (CollectionUtils.isEmpty(financePlans)) {
            return ResponseEntity.ok(warpRes);
        }
        List<PlanList> planLists = Lists.newArrayList();
        //装配结果集
        financePlans.stream().forEach(p -> {
            PlanList plan = new PlanList();
            plan.setApr(StringHelper.formatMon(p.getBaseApr() / 100D));
            plan.setId(p.getId());
            plan.setMoney(StringHelper.formatMon(p.getMoney() / 100D));
            plan.setSpend(p.getMoneyYes() / p.getMoney().doubleValue());
            plan.setTimeLimit(p.getTimeLimit());
            plan.setStatus(handleStatus(p));
            plan.setPlanName(p.getName());
            planLists.add(plan);
        });
        warpRes.setPlanLists(planLists);
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
        if (ObjectUtils.isEmpty(financePlan)) {
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
        planDetail.setLowMoney(StringHelper.formatMon(financePlan.getLowest() / 100D));
        //已投金额
        Long moneyYes = financePlan.getMoneyYes();
        //剩余金额
        planDetail.setSurplusMoney(StringHelper.formatMon((money - moneyYes) / 100D));
        planDetail.setSpend(moneyYes / money.doubleValue());
        Integer timeLimit = financePlan.getTimeLimit();
        //预期收益
        BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(money), new Double(apr), timeLimit, financePlan.getCreatedAt());
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
        //截至日期
        planDetail.setEndAt(DateHelper.dateToString(endDate));
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


}
