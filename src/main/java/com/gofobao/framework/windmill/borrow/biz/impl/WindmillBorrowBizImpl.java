package com.gofobao.framework.windmill.borrow.biz.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.windmill.borrow.biz.WindmillBorrowBiz;
import com.gofobao.framework.windmill.borrow.service.WindmillBorrowService;
import com.gofobao.framework.windmill.borrow.vo.response.BorrowTenderList;
import com.gofobao.framework.windmill.borrow.vo.response.Invest;
import com.gofobao.framework.windmill.borrow.vo.response.InvestListRes;
import com.gofobao.framework.windmill.borrow.vo.response.VoTender;
import com.gofobao.framework.windmill.util.WrbCoopDESUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/8/2.
 */
@Service
public class WindmillBorrowBizImpl implements WindmillBorrowBiz {

    @Value("${gofobao.h5Domain}")
    private String h5Address;


    @Value("${windmill.local-des-key}")
    private String localDesKey;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private WindmillBorrowService windmillBorrowService;

    @Autowired
    private BorrowService borrowService;

    /**
     * @param id
     * @return
     */
    @Override
    public InvestListRes list(Long id) {
        InvestListRes investListRes = new InvestListRes();
        do {
            try {
                List<Borrow> borrows = windmillBorrowService.list(id);
                if (CollectionUtils.isEmpty(borrows)) {
                    investListRes.setRetcode(VoBaseResp.ERROR);
                    investListRes.setRetmsg("当前没可投标");
                    break;
                }
                List<Invest> invest_list = Lists.newArrayList();
                borrows.stream().forEach(p -> {
                    try {
                        Invest invest = new Invest();
                        invest.setInvest_id(WrbCoopDESUtil.desEncrypt(localDesKey,p.getId().toString()));
                        invest.setInvest_title(p.getName());
                        invest.setInvest_url(h5Address + "#/borrow/" + p.getId());
                        invest.setTime_limit(p.getTimeLimit());
                        invest.setTime_limit_desc(p.getTimeLimit() + BorrowContants.DAY);
                        invest.setBuy_limit(p.getMost() == 0 ? "" : StringHelper.formatDouble(p.getMost() / 100D, false));
                        invest.setBuy_unit(p.getMost() == 0 ? "" : StringHelper.formatDouble(p.getMost() / 100D, false));
                        invest.setInvested_amount(StringHelper.formatMon(p.getMoneyYes() / 100D));
                        invest.setTotal_amount(StringHelper.formatMon(p.getMoney() / 100D));
                        invest.setRate(StringHelper.formatDouble(p.getApr() / 100D, false));
                        invest.setProgress(StringHelper.formatDouble(p.getMoneyYes() / p.getMoney(), false));
                        invest.setStart_time(DateHelper.dateToString(p.getVerifyAt()));
                        if (p.getRepayFashion() == 0) {
                            invest.setPayback_way(BorrowContants.REPAY_FASHION_MONTH_STR);
                        }
                        if (p.getRepayFashion() == 1) {
                            invest.setPayback_way(BorrowContants.REPAY_FASHION_ONCE_STR);
                        }
                        if (p.getRepayFashion() == 2) {
                            invest.setPayback_way(BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR);
                        }
                        invest.setInvest_condition(StringUtils.isEmpty(p.getIsNovice()) ? "新手-APP-pc" : "APP-PC");
                        invest.setProject_description(p.getDescription());
                        invest.setLose_invest(0);
                        invest_list.add(invest);
                    } catch (Exception e) {
                        e.printStackTrace();
                        investListRes.setRetmsg("平台查询异常");
                        investListRes.setRetcode(VoBaseResp.ERROR);
                        return;
                    }
                });
                if (CollectionUtils.isEmpty(invest_list)) {
                    return investListRes;
                }

                investListRes.setInvest_list(invest_list);
                investListRes.setRetmsg("查询成功");
                investListRes.setRetcode(VoBaseResp.OK);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                investListRes.setRetcode(VoBaseResp.ERROR);
                investListRes.setRetmsg("获取平台投标信息异常");
                break;
            }
        } while (false);
        return investListRes;
    }

    /**
     * @param borrowId
     * @param date
     * @return
     */
    @Override
    public BorrowTenderList tenderList(Long borrowId, String date) {
        List<Tender> tenders = windmillBorrowService.tenderList(borrowId, date);

        BorrowTenderList borrowTenderList = new BorrowTenderList();
        if (CollectionUtils.isEmpty(tenders)) {
            borrowTenderList.setRetcode(VoBaseResp.OK);
            borrowTenderList.setRetmsg("没有查询到当前标的投标记录");
            return borrowTenderList;
        }
        Set<Long> userIds = tenders.stream().map(p -> p.getId()).collect(Collectors.toSet());
        List<Users> users = usersRepository.findByIdIn(new ArrayList<>(userIds));
        Map<Long, Users> usersMap = users.stream().collect(Collectors.toMap(Users::getId, Function.identity()));
        List<VoTender> invest_list = Lists.newArrayList();

        tenders.forEach(p -> {
            VoTender tender = new VoTender();
            try {
                tender.setIndex(WrbCoopDESUtil.desEncrypt(localDesKey, p.getId().toString()));
                tender.setInvest_money(StringHelper.formatDouble(p.getValidMoney(), false));
                tender.setInvest_time(DateHelper.dateToString(p.getCreatedAt()));
                Users tempUser = usersMap.get(p.getUserId());
                tender.setInvest_user(StringUtils.isEmpty(tempUser.getUsername()) ? UserHelper.hideChar(tempUser.getPhone(), UserHelper.PHONE_NUM) : tempUser.getUsername());
                invest_list.add(tender);
            } catch (Exception e) {
                borrowTenderList.setRetcode(VoBaseResp.ERROR);
                borrowTenderList.setRetmsg("平台查询异常");
                return;
            }
        });
        if (CollectionUtils.isEmpty(invest_list)) {
            return borrowTenderList;
        }

        borrowTenderList.setRetcode(VoBaseResp.OK);
        borrowTenderList.setRetmsg("查询成功");
        borrowTenderList.setFirst_invest_time(invest_list.get(0).getInvest_time());
        borrowTenderList.setLast_invest_time(invest_list.get(invest_list.size() - 1).getInvest_time());
        Borrow borrow = borrowService.findByBorrowId(borrowId);
        borrowTenderList.setAll_investors(borrow.getTenderCount());
        borrowTenderList.setInvest_list(invest_list);
        return borrowTenderList;
    }
}
