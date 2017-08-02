package com.gofobao.framework.windmill.borrow.biz.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.windmill.borrow.biz.WindmillBorrowBiz;
import com.gofobao.framework.windmill.borrow.service.WindmillBorrowService;
import com.gofobao.framework.windmill.borrow.vo.response.Invest;
import com.gofobao.framework.windmill.borrow.vo.response.InvestListRes;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by admin on 2017/8/2.
 */
public class WindmillBorrowBizImpl implements WindmillBorrowBiz {

    @Value("{gofobao.h5Domain}")
    private String h5Address;


    @Autowired
    private WindmillBorrowService borrowService;

    /**
     *
     * @param id
     * @return
     */
    @Override
    public InvestListRes list(Long id) {

        InvestListRes investListRes = new InvestListRes();
        try {
            List<Borrow> borrows = borrowService.list(id);
            if (CollectionUtils.isEmpty(borrows)) {
                investListRes.setRetcode(VoBaseResp.ERROR);
                investListRes.setRetmsg("当前没可投标");
            }
            List<Invest> invest_list = Lists.newArrayList();
            borrows.stream().forEach(p -> {
                Invest invest = new Invest();
                invest.setInvest_id(p.getId());
                invest.setInvest_title(p.getName());
                invest.setInvest_url(h5Address);
                invest.setTime_limit(p.getTimeLimit());
                invest.setTime_limit_desc(p.getTimeLimit() + BorrowContants.DAY);
                invest.setBuy_limit(StringUtils.isEmpty(p.getMost()) ? "" : StringHelper.formatMon(p.getMost() / 100D));
                invest.setBuy_unit(StringUtils.isEmpty(p.getMost()) ? "" : StringHelper.formatMon(p.getMost() / 100D));
                invest.setInvested_amount(StringHelper.formatMon(p.getMoneyYes() / 100D));
                invest.setTotal_amount(StringHelper.formatMon(p.getMoney() / 100D));
                invest.setRate(StringHelper.formatMon(p.getApr() / 100D));
                invest.setProgress(StringHelper.formatMon(p.getMoneyYes() / p.getMoney()));
                invest.setStart_time(DateHelper.dateToString(p.getVerifyAt()));
                if (p.getRepayFashion() == 1) {
                    invest.setPayback_way(BorrowContants.REPAY_FASHION_MONTH_STR);
                }
                if (p.getRepayFashion() == 2) {
                    invest.setPayback_way(BorrowContants.REPAY_FASHION_ONCE_STR);
                }
                if (p.getRepayFashion() == 3) {
                    invest.setPayback_way(BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR);
                }
                invest.setInvest_condition(StringUtils.isEmpty(p.getIsNovice()) ? "新手-APP-pc" : "APP-PC");
                invest.setProject_description(p.getDescription());
                invest.setLose_invest(0);
                invest_list.add(invest);
            });
            investListRes.setInvest_list(invest_list);
            investListRes.setRetmsg("查询成功");
            investListRes.setRetcode(VoBaseResp.OK);
        } catch (Exception e) {
            investListRes.setRetcode(VoBaseResp.ERROR);
            investListRes.setRetmsg("获取平台投标信息异常");

        }
        return investListRes;
    }
}
