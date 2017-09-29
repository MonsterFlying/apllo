package com.gofobao.framework.starfire.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.starfire.borrow.biz.StarFireBorrowBiz;
import com.gofobao.framework.starfire.common.request.BaseRequest;
import com.gofobao.framework.starfire.common.response.CodeTypeConstant;
import com.gofobao.framework.starfire.common.response.ResultCodeEnum;
import com.gofobao.framework.starfire.common.response.ResultCodeMsgEnum;
import com.gofobao.framework.starfire.tender.constants.StarFireBorrowConstant;
import com.gofobao.framework.starfire.tender.vo.request.BorrowsQuery;
import com.gofobao.framework.starfire.tender.vo.response.BorrowQueryRes;
import com.gofobao.framework.starfire.util.AES;
import com.gofobao.framework.starfire.util.SignUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by master on 2017/9/29.
 */
@Service
@Slf4j
public class StarFireBorrowBizImpl implements StarFireBorrowBiz {

    @Value("${starfire.key}")
    private static String key;

    @Value("${starfire.initVector}")
    private static String initVector;

    @Autowired
    private BorrowService borrowService;

    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${gofobao.pcDomain}")
    private String pcDomain;

    @Autowired
    private BaseRequest baseRequest;

    /**
     * 对接平台标的列表查询
     *
     * @param borrowsQuery
     * @return
     */
    @Override
    public BorrowQueryRes queryBorrows(BorrowsQuery borrowsQuery) {

        //封装验签参数
        baseRequest.setT_code(borrowsQuery.getT_code());
        baseRequest.setC_code(borrowsQuery.getC_code());
        baseRequest.setSerial_num(borrowsQuery.getSerial_num());
        baseRequest.setSign(borrowsQuery.getSign());

        //封装返回参数
        BorrowQueryRes borrowsResult = new BorrowQueryRes();
        borrowsResult.setSerial_num(borrowsQuery.getSerial_num());
        //验签结果
        if (!SignUtil.checkSign(baseRequest, key, initVector)) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
            borrowsResult.setResult(ResultCodeMsgEnum.getResultMsg(code));
            return borrowsResult;
        }
        try {
            String bidIds = borrowsQuery.getBid_id();
            List<String> borrowIds = null;
            if (!StringUtils.isEmpty(bidIds)) {
                borrowIds = Lists.newArrayList(AES.decrypt(bidIds, key, initVector).split(";"));
            }
            Specification<Borrow> borrowSpecification;
            if (CollectionUtils.isEmpty(borrowIds)) {
                borrowSpecification = Specifications.<Borrow>and()
                        .in("id", borrowIds.toArray())
                        .eq("isWindmill", true)
                        .build();
            } else {
                borrowSpecification = Specifications.<Borrow>and()
                        .eq("isStarFire", true)
                        .eq("status", BorrowContants.BIDDING)
                        .eq("successAt", null)
                        .build();
            }
            List<Borrow> borrows = borrowService.findList(borrowSpecification,
                    new Sort(Sort.Direction.DESC,
                            "releaseAt"));
            if (CollectionUtils.isEmpty(borrows)) {
                String code = ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS);
                borrowsResult.setResult(code);
                borrowsResult.setErr_msg("当前没有可投的标的");
                return borrowsResult;
            }
            Integer borrowSize = borrows.size();
            List<BorrowQueryRes.Records> records = new ArrayList<>(borrowSize);
            borrows.forEach(borrow -> {
                Long borrowId = borrow.getId();
                BorrowQueryRes.Records record = borrowsResult.new Records();
                record.setBid_id(borrowId);
                record.setBid_name(borrow.getName());
                record.setBid_type(borrow.getBorrowTypeStr());
                record.setGuarantee_type(borrow.getType() == BorrowContants.CE_DAI ? "汽车抵押" : "");
                Long money = borrow.getMoney();
                Long moneyYes = borrow.getMoneyYes();
                record.setBorrow_amount(StringHelper.formatDouble(money / 100D, false));
                record.setLeft_amount(StringHelper.formatDouble((money - moneyYes) / 100D, false));
                record.setBid_rate(StringHelper.formatMon(borrow.getApr() / 100d));
                record.setRaise_rate("0.00");
                record.setInterest_date(!StringUtils.isEmpty(borrow.getRecheckAt())
                        ? DateHelper.dateToString(borrow.getRecheckAt())
                        : "");
                record.setRepay_type(borrow.getBorrowBackWayStr());
                record.setRepay_count(borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE
                        ? "1"
                        : borrow.getTimeLimit().toString());
                record.setBid_status(getBorrowStatus(borrow));
                record.setBond_code(borrowId.toString());
                record.setBid_url(pcDomain + "/borrow/" + borrowId);
                record.setWap_bid_url(h5Domain + "/#/borrow/" + borrowId);
                record.setIsPromotion(false);
                record.setIsRecommend(false);
                record.setIsNovice(borrow.getIsNovice());
                record.setIsExclusive(false);
                record.setIsAssignment(false);
                record.setCanAssign(true);
                Double progress = Double.valueOf(StringHelper.formatDouble(
                        moneyYes / borrow.getMoney().doubleValue(),
                        false)) * 100;
                record.setBid_progress_percent(progress.toString());
                record.setIntroduction(borrow.getDescription());
                if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
                    record.setIsDurationMonths(false);
                    record.setDuration_days(borrow.getTimeLimit().toString());
                } else {
                    record.setDuration_months(borrow.getTimeLimit().toString());
                }
                records.add(record);
            });
            borrowsResult.setRecords(records);
            return borrowsResult;
        } catch (Exception e) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
            borrowsResult.setResult(ResultCodeMsgEnum.getResultMsg(code));
            return borrowsResult;
        }
    }

    /**
     * 标的状态
     * @param borrow
     * @return
     */
    private String getBorrowStatus(Borrow borrow) {
        if (borrow.getStatus() == BorrowContants.BIDDING) {
            if (!StringUtils.isEmpty(borrow.getSuccessAt())) {
                return StarFireBorrowConstant.SHENHEZHONG;
            }
            Date endAt = DateHelper.addDays(borrow.getReleaseAt(), borrow.getValidDay() + 1);
            if (endAt.getTime() < new Date().getTime()) {
                return StarFireBorrowConstant.LIUBIAO;
            } else {
                return StarFireBorrowConstant.WEIMIANBIAO;
            }

        } else if (borrow.getStatus() == BorrowContants.PASS) {
            if (StringUtils.isEmpty(borrow.getCloseAt())) {
                return StarFireBorrowConstant.YIJIEQING;
            } else {
                return StarFireBorrowConstant.HUANKUANZHONG;
            }

        }
        return "";
    }


}
