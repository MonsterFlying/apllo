package com.gofobao.framework.starfire.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
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
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Range;
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
    private String key;

    @Value("${starfire.initVector}")
    private String initVector;

    @Autowired
    private BorrowService borrowService;


    private static Gson GSON = new Gson();

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

        log.info("============进入标的查询接口==============");
        log.info("打印星火请求信息：" + GSON.toJson(borrowsQuery));
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
            Date nowDate = new Date();
            String bidIds = borrowsQuery.getBid_id();
            List<String> borrowIds = null;
            if (!StringUtils.isEmpty(bidIds)) {
                borrowIds = Lists.newArrayList(AES.decrypt(key, initVector, bidIds).split(","));
            }
            Specification<Borrow> borrowSpecification;
            Integer pageSize = 0;
            if (!CollectionUtils.isEmpty(borrowIds)) {
                borrowSpecification = Specifications.<Borrow>and()
                        .in("id", borrowIds.toArray())
                        .eq("isWindmill", true)
                        .build();
                pageSize = borrowIds.size();
            } else {
                borrowSpecification = Specifications.<Borrow>and()
                        .eq("isWindmill", true)
                        .eq("status", BorrowContants.BIDDING)
                        .eq("successAt", null)
                        .between("releaseAt", new Range<>(DateHelper.subDays(nowDate, 10),
                                DateHelper.endOfDate(nowDate)))
                        .build();
                pageSize = 100;
            }
            List<Borrow> borrows = borrowService.findList(borrowSpecification,
                    new PageRequest(0,
                            pageSize,
                            new Sort(Sort.Direction.DESC,
                                    "releaseAt")
                    ));
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
                record.setWap_bid_url("/#/borrow/" + borrowId);
                record.setBid_url("/borrow/" + borrowId);
                record.setIsPromotion(false);
                record.setIsRecommend(false);
                record.setIsNovice(borrow.getIsNovice());
                record.setIsExclusive(false);
                record.setIsAssignment(false);
                record.setCanAssign(true);
                String progress = StringHelper.formatMon(
                        NumberHelper.floorDouble(borrow.getMoneyYes() / borrow.getMoney().doubleValue(),
                                2)
                                * 100);
                record.setBid_progress_percent(progress);
                record.setIntroduction(borrow.getDescription());
                if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
                    record.setIsDurationMonths(false);
                    record.setDuration_days(borrow.getTimeLimit().toString());
                } else {
                    record.setDuration_months(borrow.getTimeLimit().toString());
                }
                records.add(record);
            });
            borrowsResult.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
            borrowsResult.setRecords(records);
            return borrowsResult;
        } catch (Exception e) {
            log.error("标的查询失败,打印错误信息:", e);
            String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
            borrowsResult.setResult(ResultCodeMsgEnum.getResultMsg(code));
            return borrowsResult;
        }
    }

    /**
     * 标的状态
     *
     * @param borrow
     * @return
     */
    private String getBorrowStatus(Borrow borrow) {
        if (borrow.getStatus() == BorrowContants.BIDDING) {
            Date endAt = DateHelper.addDays(borrow.getReleaseAt(), borrow.getValidDay() + 1);
            if (!StringUtils.isEmpty(borrow.getSuccessAt())) {
                return StarFireBorrowConstant.SHENHEZHONG;
            } else if (endAt.getTime() < new Date().getTime()) {
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
