package com.gofobao.framework.wheel.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.wheel.borrow.biz.WheelBorrowBiz;
import com.gofobao.framework.wheel.borrow.vo.request.BorrowsReq;
import com.gofobao.framework.wheel.borrow.vo.request.InvestNoticeReq;
import com.gofobao.framework.wheel.borrow.vo.response.BorrowsRes;
import com.gofobao.framework.wheel.common.BaseResponse;
import com.gofobao.framework.wheel.common.ResponseConstant;
import com.gofobao.framework.wheel.util.JEncryption;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author master
 * @date 2017/10/27
 */
@Slf4j
@Service
public class WheelBorrowBizImpl implements WheelBorrowBiz {

    @Value("${wheel.domain}")
    private String wheelDomain;

    @Value("${wheel.short-name}")
    private String shortName;

    @Value("${wheel.secret-key}")
    private String secretKey;

    @Autowired
    private Gson GSON;

    @Autowired
    private UserService userService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    /**
     * 5.1标的查询接口
     *
     * @param borrow
     * @return
     */
    @Override
    public BorrowsRes borrows(BorrowsReq borrow) {
        BorrowsRes borrowsRes = new BorrowsRes();
        try {
            List<Borrow> borrows;
            if (ObjectUtils.isEmpty(borrow) || StringUtils.isEmpty(borrow.getInvest_id())) {
                Specification<Borrow> borrowSpecification = Specifications.<Borrow>and()
                        .in("status", Lists.newArrayList(BorrowContants.BIDDING,BorrowContants.PENDING).toArray())
                        .eq("successAt", null)
                        .eq("isWindmill", true)
                        .build();
                borrows = borrowService.findList(borrowSpecification);
            } else {
                borrows = new ArrayList<>(1);
                borrows.add(borrowService.findByBorrowId(Long.valueOf(borrow.getInvest_id())));
            }
            if (CollectionUtils.isEmpty(borrows)) {
                borrowsRes.setRetcode(ResponseConstant.SUCCESS);
                return borrowsRes;
            }
            List<BorrowsRes.BorrowInfo> infoArrayList = new ArrayList<>(borrows.size());
            borrows.forEach(tempBorrow -> {
                infoArrayList.add(commonHandle(tempBorrow));
            });
            borrowsRes.setInvest_list(infoArrayList);
            borrowsRes.setRetcode(ResponseConstant.SUCCESS);
            return borrowsRes;
        } catch (Exception e) {
            borrowsRes.setRetcode(ResponseConstant.FAIL);
            borrowsRes.setRetmsg("平台标的列表查询异常");
            return borrowsRes;
        }
    }

    /**
     * 4.2 标的变化接口通知接口
     *
     * @return
     */
    @Override
    public void borrowUpdateNotice(Borrow borrow) {
        String borrowUpdateNoticeUrl = "/financial/ps_target_notice";
        try {
            BorrowsRes.BorrowInfo borrowInfo = commonHandle(borrow);
            Map<String, String> paramMaps = GSON.fromJson(GSON.toJson(borrowInfo),
                    new TypeToken<Map<String, String>>() {
                    }.getType());
            String paramStr = "";
            for (String keyStr : paramMaps.keySet()) {
                paramStr += keyStr + "=" + paramMaps.get(keyStr) + "&";
            }
            String requestParamStr = paramStr.substring(0, paramStr.lastIndexOf("&"));

            String bizParamStr = JEncryption.encrypt(requestParamStr.getBytes("utf-8"), secretKey);
            String param = "?param=" + URLEncoder.encode(bizParamStr, "utf-8") + "&from=" + shortName;
            log.info("打印请求车轮参数：" + param);
            System.out.print("标的变化接口通知,请求车轮地址：" + wheelDomain + borrowUpdateNoticeUrl + param);

            String resultStr = OKHttpHelper.get(wheelDomain + borrowUpdateNoticeUrl + param, null, null);
            log.info("打印车轮返回信息：" + resultStr);
            if (StringUtils.isEmpty(resultStr)) {
                log.info("请求车轮异常");
                return;
            }
            BaseResponse baseResponse = GSON.fromJson(resultStr,
                    new TypeToken<BaseResponse>() {
                    }.getType());
            log.info(baseResponse.getRetcode().equals(ResponseConstant.SUCCESS)
                    ? "标的变化通知车轮成功" : baseResponse.getRetmsg());
        } catch (Exception e) {
            log.info("请求车轮异常", e);
        }
    }

    /**
     * 公共处理方法
     *
     * @param tempBorrow
     * @return
     */
    private BorrowsRes.BorrowInfo commonHandle(Borrow tempBorrow) {
        BorrowsRes borrowsRes = new BorrowsRes();
        BorrowsRes.BorrowInfo borrowInfo = borrowsRes.new BorrowInfo();
        borrowInfo.setInvest_id(tempBorrow.getId());
        borrowInfo.setInvest_title(tempBorrow.getName());
        borrowInfo.setBuy_unit(StringHelper.formatDouble(tempBorrow.getLowest() / 100, false));
        borrowInfo.setBuy_limit(StringHelper.formatDouble(tempBorrow.getMost() / 100, false));
        borrowInfo.setInvest_url("/#/borrow/" + tempBorrow.getId());
        Integer repayFashion = tempBorrow.getRepayFashion();
        borrowInfo.setTime_limit(repayFashion.equals(BorrowContants.REPAY_FASHION_ONCE)
                ? tempBorrow.getTimeLimit()
                : tempBorrow.getTimeLimit() * 30);
        borrowInfo.setTime_limit_desc(repayFashion.equals(BorrowContants.REPAY_FASHION_ONCE)
                ? tempBorrow.getTimeLimit() + BorrowContants.DAY
                : tempBorrow.getTimeLimit() + BorrowContants.MONTH);
        borrowInfo.setTotal_amount(StringHelper.formatDouble(tempBorrow.getMoney() / 100d, false));
        borrowInfo.setRate(StringHelper.formatDouble(tempBorrow.getApr() / 100D, false));

        String progress = StringHelper.formatMon(
                NumberHelper.floorDouble(tempBorrow.getMoneyYes() / tempBorrow.getMoney().doubleValue(),
                        2)
                        * 100);
        borrowInfo.setProgress(progress);
        borrowInfo.setPayback_way(repayFashion.equals(BorrowContants.REPAY_FASHION_ONCE)
                ? "一次性还本付息"
                : repayFashion.equals(BorrowContants.REPAY_FASHION_MONTH)
                ? "等额本息"
                : "按月付息");
        borrowInfo.setInvest_condition(tempBorrow.getIsNovice() ? "新手" : "");
        borrowInfo.setProject_description(tempBorrow.getDescription());
        if (tempBorrow.getStatus().intValue() == BorrowContants.CANCEL) {
            borrowInfo.setLose_invest(1);
        }
        return borrowInfo;
    }

    /**
     * 用户投资通知车轮
     *
     * @param tender
     */
    @Override
    public void investNotice(Tender tender) {
        log.info("=======================================");
        log.info("===========进入通知车轮理财接口==========");
        log.info("=======================================");
        Users user = userService.findById(tender.getUserId());
        if (StringUtils.isEmpty(user.getWheelId())) {
            log.info("当前用户不是车轮用户");
            return;
        }
        Borrow borrow = borrowService.findById(tender.getBorrowId());
        Long userId = user.getId();
        InvestNoticeReq investNotice = new InvestNoticeReq();
        investNotice.setPf_user_id(userId.toString());
        Asset asset = assetService.findByUserId(userId);
        Long noUseMoney = asset.getNoUseMoney();
        Long useMoney = asset.getUseMoney();
        Long collection = asset.getCollection();
        investNotice.setAll_balance(StringHelper.formatDouble(useMoney + noUseMoney + collection, 100, false));
        investNotice.setAvailable_balance(StringHelper.formatDouble(useMoney, 100, false));
        investNotice.setFrozen_money(StringHelper.formatDouble(noUseMoney, 100, false));
        investNotice.setReward("0");
        UserCache userCache = userCacheService.findById(userId);
        Long waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Long waitCollectionInterest = userCache.getWaitCollectionInterest();
        investNotice.setInvesting_interest(StringHelper.formatDouble(waitCollectionInterest, 100, false));
        investNotice.setInvesting_principal(StringHelper.formatDouble(waitCollectionPrincipal, 100, false));
        investNotice.setEarned_interest(StringHelper.formatDouble(userCache.getIncomeTotal(), 100, false));
        investNotice.setCurrent_money(StringHelper.formatDouble(0, false));
        investNotice.setInvest_record_id(tender.getId().toString());
        investNotice.setProject_title(borrow.getName());
        investNotice.setProject_id(borrow.getId().toString());
        investNotice.setProject_url("/#/borrow/" + borrow.getId());
        investNotice.setProject_rate(StringHelper.formatDouble(borrow.getApr(), 100, false));
        String progress = StringHelper.formatMon(
                NumberHelper.floorDouble(borrow.getMoneyYes() / borrow.getMoney().doubleValue(),
                        2)
                        * 100);
        investNotice.setProject_progress(progress);
        Integer repayFashion = borrow.getRepayFashion();
        investNotice.setProject_timelimit(repayFashion.equals(BorrowContants.REPAY_FASHION_ONCE)
                ? borrow.getTimeLimit()
                : borrow.getTimeLimit() * 30);
        investNotice.setProject_timelimit_desc(repayFashion.equals(BorrowContants.REPAY_FASHION_ONCE)
                ? borrow.getTimeLimit() + BorrowContants.DAY
                : borrow.getTimeLimit() + BorrowContants.MONTH);

        investNotice.setPayback_way(repayFashion.equals(BorrowContants.REPAY_FASHION_ONCE)
                ? "一次性还本付息"
                : repayFashion.equals(BorrowContants.REPAY_FASHION_MONTH)
                ? "等额本息"
                : "按月付息");
        investNotice.setInvest_money(StringHelper.formatDouble(tender.getValidMoney(), 100, false));
        if (!StringUtils.isEmpty(borrow.getRecheckAt())
                && BorrowContants.PASS.equals(borrow.getStatus())
                && StringUtils.isEmpty(borrow.getCloseAt())) {
            Specification<BorrowRepayment> specification = Specifications.<BorrowRepayment>and()
                    .eq("borrowId", borrow.getId())
                    .build();
            List<BorrowRepayment> borrowRepayments = borrowRepaymentService.findList(specification,
                    new Sort(Sort.Direction.ASC, "order"));
            BorrowRepayment borrowRepayment = borrowRepayments.stream()
                    .filter(p -> p.getStatus().equals(RepaymentContants.STATUS_NO))
                    .collect(Collectors.toList())
                    .get(0);
            investNotice.setMonthly_back_date(DateHelper.getDay(borrow.getReleaseAt()));
            investNotice.setNext_back_date(DateHelper.dateToString(borrowRepayment.getRepayAt(), DateHelper.DATE_FORMAT_YMD));
            investNotice.setNext_back_money(StringHelper.formatDouble(borrowRepayment.getRepayMoney(), 100, false));
            investNotice.setNext_back_interest(StringHelper.formatDouble(borrowRepayment.getInterest(), 100, false));
            investNotice.setNext_back_principal(StringHelper.formatDouble(borrowRepayment.getPrincipal(), 100, false));
        }
        Integer transferFlag = tender.getTransferFlag();
        if (transferFlag.equals(TenderConstans.TRANSFER_PART_YES) || transferFlag.equals(TenderConstans.TRANSFER_YES)) {
            investNotice.setAttorn_state(1);
            investNotice.setAttorn_time(DateHelper.dateToString(tender.getUpdatedAt()));
        }
        investNotice.setInterest_time("");
        if (tender.getState().equals(TenderConstans.BACK_MONEY)) {
            investNotice.setInterest_time(DateHelper.dateToString(borrow.getRecheckAt()));
        } else if (tender.getState().equals(TenderConstans.SETTLE)) {
            investNotice.setInvest_status(1);
            investNotice.setInterest_time(DateHelper.dateToString(borrow.getRecheckAt()));
        }
        investNotice.setInvest_time(DateHelper.dateToString(tender.getCreatedAt()));
        investNotice.setInvest_title(borrow.getName());
        String investNoticeUrl = "/financial/ps_invest_notice";
        Map<String, String> paramMap = GSON.fromJson(GSON.toJson(investNotice),
                new TypeToken<HashMap<String, String>>() {
                }.getType());

        String paramStr = "";
        for (String keyStr : paramMap.keySet()) {
            paramStr += keyStr + "=" + paramMap.get(keyStr) + "&";
        }
        String tempRequestParamStr = paramStr.substring(0, paramStr.lastIndexOf("&"));
        try {
            String bizParamStr = JEncryption.encrypt(tempRequestParamStr.getBytes("utf-8"), secretKey);
            String param = "?param=" + URLEncoder.encode(bizParamStr, "utf-8") + "&from=" + shortName;
            System.out.print("用户投资通知车轮,请求车轮地址：" + wheelDomain + investNoticeUrl + param);
            String resultStr = OKHttpHelper.get(wheelDomain + investNoticeUrl + param, null, null);
            if (StringUtils.isEmpty(resultStr)) {
                log.info("请求车轮异常");
                return;
            }
            BaseResponse baseResponse = GSON.fromJson(resultStr,
                    new TypeToken<BaseResponse>() {
                    }.getType());

            log.info(baseResponse.getRetcode().equals(ResponseConstant.SUCCESS)
                    ? "用户投资通知车轮成功" : baseResponse.getRetmsg());
        } catch (Exception e) {
            log.info("用户投资通知车轮失败", e);
        }
    }
}
