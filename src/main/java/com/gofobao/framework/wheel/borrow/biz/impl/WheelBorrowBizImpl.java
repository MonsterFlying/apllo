package com.gofobao.framework.wheel.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.wheel.borrow.biz.WheelBorrowBiz;
import com.gofobao.framework.wheel.borrow.vo.request.BorrowsReq;
import com.gofobao.framework.wheel.borrow.vo.response.BorrowUpdateRes;
import com.gofobao.framework.wheel.borrow.vo.response.BorrowsRes;
import com.gofobao.framework.wheel.common.ResponseConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author master
 * @date 2017/10/27
 */
public class WheelBorrowBizImpl implements WheelBorrowBiz {

    @Autowired
    private BorrowService borrowService;

    @Value("${wheel.domain}")
    private String wheelDomain;


    @Value("${wheel.short-name}")
    private String shortName;


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
                        .eq("status", BorrowContants.BIDDING)
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
    public BorrowUpdateRes borrowUpdateNotice(Borrow borrow) {
        String borrowUpdateNoiticeUrl = "/financial/ps_target_notice";
        BorrowsRes.BorrowInfo borrowInfo = commonHandle(borrow);

        //     OKHttpHelper.postForm(wheelDomain+borrowUpdateNoiticeUrl,);
        return null;

    }


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
        borrowInfo.setProgress(progress + BorrowContants.PERCENT);
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


}
