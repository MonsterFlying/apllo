package com.gofobao.framework.helper.project;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 理财计算器
 * Created by Max on 2017/2/28.
 */
public class BorrowCalculatorHelper {
    private Double principal;   //本金
    private Double msPrincipal; //剩余本金
    private Double yearApr;     //年利率
    private Double monthApr;    //月利率
    private Double dayApr;      //天利率
    private Integer timeLimit;  //借款期限
    private Date successAt;
    private Double repayTotal;  //全部偿还
    private Double eachRepay;   //每期应还

    /**
     * 初始化操作
     *
     * @param principal 本金 （分）
     * @param yearApr   年利率 13.32% --> 1332
     * @param timeLimit 借款期限
     * @param successAt 满标时间
     */
    public BorrowCalculatorHelper(Double principal, Double yearApr, Integer timeLimit, Date successAt) {
        if (principal <= 0 || yearApr <= 0 || timeLimit < 1) {
            return;
        }

        this.principal = principal;
        this.msPrincipal = this.principal;
        this.yearApr = yearApr / 100 / 100;
        this.monthApr = this.yearApr / 12;
        this.dayApr = this.yearApr / 365;
        this.timeLimit = timeLimit;
        this.successAt = ObjectUtils.isEmpty(successAt) ? new Date() : successAt;
    }

    /**
     * 快速计算
     *
     * @param repayFashion 还款方式 0等额本息  1一次性还本付息  2：先息后本
     * @return
     */
    public Map<String, Object> simpleCount(int repayFashion) {
        Map<String, Object> rs = null;
        switch (repayFashion) {
            case 0:
                rs = this.dengEBenXi();
                break;
            case 1:
                rs = this.ycxhbfx();
                break;
            case 2:
                rs = this.dqhbayfx();
                break;
            default:
        }
        return rs;
    }

    /**
     * 每月等额本息
     * 月均还款 = 贷款本金 × 月利率 × ( 1 + 月利率) ^ 还款月数 / [ (1+月利率) ^ 还款月数 - 1]
     * a*[i*(1+i)^n]/[(1+I)^n-1]
     * (a×i－b)×(1＋i)
     *
     * @return
     */
    public Map<String, Object> dengEBenXi() {
        Map<String, Object> rsMap = null;
        do {
            rsMap = new HashMap<>();
            double li = MathHelper.pow((1 + this.monthApr), this.timeLimit);
            this.eachRepay = MathHelper.myRound(this.principal * this.monthApr * li / (li - 1), 0);//月均还款
            rsMap.put("eachRepay", this.eachRepay);     // 每期还款
            this.repayTotal = this.eachRepay * this.timeLimit;
            rsMap.put("repayTotal", this.repayTotal);   // 总额偿还
            rsMap.put("earnings", MathHelper.myRound(this.repayTotal - this.principal, 0));   // 总利息
            List<Map<String, String>> repayDetailList = new ArrayList<>();
            rsMap.put("repayDetailList", repayDetailList);
            Map<String, String> repayDetailMap = null;
            double interest = 0;
            double principal = 0;
            Date repayAt = null;
            int month = 0;
            for (int i = 0; i < this.timeLimit; i++) {
                repayDetailMap = new HashMap<>();
                interest = MathHelper.myRound(this.msPrincipal * this.monthApr, 0);
                principal = this.eachRepay - interest;
                if (i + 1 == this.timeLimit) {
                    principal = this.msPrincipal;
                    interest = MathHelper.max(MathHelper.myRound(this.eachRepay - principal, 0), 0);
                    if (interest == 0) {
                        interest = this.eachRepay;
                    }
                }

                principal = MathHelper.myRound(principal, 0);
                this.msPrincipal = MathHelper.myRound(this.msPrincipal - principal, 0);

                repayAt = DateHelper.addMonths((Date) this.successAt.clone(), i + 1);

                repayDetailMap.put("repayMoney", NumberHelper.toString(MathHelper.myRound(principal + interest, 0)));
                repayDetailMap.put("principal", NumberHelper.toString(principal));
                repayDetailMap.put("interest", NumberHelper.toString(interest));
                repayDetailMap.put("msPrincipal", NumberHelper.toString(this.msPrincipal));
                repayDetailMap.put("repayAt", DateHelper.dateToString(repayAt));
                repayDetailList.add(repayDetailMap);
            }
        } while (false);
        return rsMap;
    }

    /**
     * 一次性还本付息
     *
     * @return
     */
    public Map<String, Object> ycxhbfx() {
        Map<String, Object> rsMap = new HashMap<>();
        double interest = MathHelper.myRound(this.msPrincipal * this.dayApr * this.timeLimit, 0);
        double principal = this.msPrincipal;

        this.msPrincipal = MathHelper.myRound(this.msPrincipal - principal, 0);
        this.eachRepay = this.repayTotal = MathHelper.myRound(principal + interest, 0);
        rsMap.put("eachRepay", this.eachRepay);
        rsMap.put("repayTotal", this.repayTotal);
        rsMap.put("earnings", MathHelper.myRound(this.repayTotal - this.principal, 0));

        Date repayAt = DateHelper.addDays((Date) this.successAt.clone(), timeLimit);

        List<Map<String, String>> repayDetailList = new ArrayList<>();
        rsMap.put("repayDetailList", repayDetailList);
        Map<String, String> repayDetailMap = new HashMap<>();
        repayDetailMap.put("repayMoney", NumberHelper.toString(MathHelper.myRound(principal + interest, 0)));
        repayDetailMap.put("principal", NumberHelper.toString(principal));
        repayDetailMap.put("interest", NumberHelper.toString(interest));
        repayDetailMap.put("msPrincipal", NumberHelper.toString(this.msPrincipal));
        repayDetailMap.put("repayAt", DateHelper.dateToString(repayAt));
        repayDetailList.add(repayDetailMap);

        return rsMap;
    }

    /**
     * 到期还本，按月付息
     *
     * @return
     */
    public Map<String, Object> dqhbayfx() {
        Map<String, Object> rsMap = new HashMap<>();
        double interest = MathHelper.myRound(this.msPrincipal * this.monthApr, 0);
        this.eachRepay = interest;
        this.repayTotal = MathHelper.myRound(this.principal + interest * this.timeLimit, 0);
        rsMap.put("eachRepay", this.eachRepay);
        rsMap.put("repayTotal", this.repayTotal);
        rsMap.put("earnings", MathHelper.myRound(this.repayTotal - this.principal, 0));

        double principal = 0;
        Date repayAt = null;
        List<Map<String, String>> repayDetailList = new ArrayList<>();
        rsMap.put("repayDetailList", repayDetailList);
        Map<String, String> repayDetailMap = null;

        for (int i = 0; i < this.timeLimit; i++) {
            repayDetailMap = new HashMap<>();
            principal = 0;

            if (i + 1 == this.timeLimit) {
                principal = this.msPrincipal;
            }

            this.msPrincipal = MathHelper.myRound(this.msPrincipal - principal, 0);

            //操作时间
            repayAt = DateHelper.addMonths((Date) this.successAt.clone(), i + 1);

            repayDetailMap.put("repayMoney", NumberHelper.toString(MathHelper.myRound(principal + interest, 0)));
            repayDetailMap.put("principal", NumberHelper.toString(principal));
            repayDetailMap.put("interest", NumberHelper.toString(interest));
            repayDetailMap.put("msPrincipal", NumberHelper.toString(this.msPrincipal));
            repayDetailMap.put("repayAt", DateHelper.dateToString(repayAt));
            repayDetailList.add(repayDetailMap);
        }

        return rsMap;
    }

    public Double getPrincipal() {
        return principal;
    }

    public void setPrincipal(Double principal) {
        this.principal = principal;
    }

    public Double getMsPrincipal() {
        return msPrincipal;
    }

    public void setMsPrincipal(Double msPrincipal) {
        this.msPrincipal = msPrincipal;
    }

    public Double getYearApr() {
        return yearApr;
    }

    public void setYearApr(Double yearApr) {
        this.yearApr = yearApr;
    }

    public Double getMonthApr() {
        return monthApr;
    }

    public void setMonthApr(Double monthApr) {
        this.monthApr = monthApr;
    }

    public Double getDayApr() {
        return dayApr;
    }

    public void setDayApr(Double dayApr) {
        this.dayApr = dayApr;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Date getSuccessAt() {
        return successAt;
    }

    public void setSuccessAt(Date successAt) {
        this.successAt = successAt;
    }

    public Double getRepayTotal() {
        return repayTotal;
    }

    public void setRepayTotal(Double repayTotal) {
        this.repayTotal = repayTotal;
    }

    public Double getEachRepay() {
        return eachRepay;
    }

    public void setEachRepay(Double eachRepay) {
        this.eachRepay = eachRepay;
    }

}
