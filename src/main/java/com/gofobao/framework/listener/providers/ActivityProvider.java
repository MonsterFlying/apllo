package com.gofobao.framework.listener.providers;

import com.gofobao.framework.award.entity.Coupon;
import com.gofobao.framework.award.service.CouponService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.PhoneHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by Zeke on 2017/6/20.
 */
@Component
@Slf4j
public class ActivityProvider {

    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserService userService;
    @Autowired
    private CouponService couponService;

    /**
     * 赠送流量券队列
     *
     * @param paramMap
     * @throws Exception
     */
    public void TenderGiveTrafficCoupon(Map<String, String> paramMap) throws Exception {
        Long tenderId = NumberHelper.toLong(paramMap.get("tenderId"));
        if (StringUtils.isEmpty(tenderId)) {
            log.error("赠送流量券MQ：参数缺失！");
            return;
        }

        Tender tender = tenderService.findById(tenderId);
        Users user = userService.findById(tender.getUserId());

        String phone = user.getPhone();
        if (ObjectUtils.isEmpty(phone)) {
            log.error("赠送流量券MQ：会员手机号码不存在！");
            return;
        }

        Long validMoney = tender.getValidMoney();
        if (validMoney < 1000 * 100) {//单位：分
            log.error("赠送流量券MQ：有效投标金额没有达到活动最低要求");
        }

        int code = PhoneHelper.getTelCorpCode(phone);
        if (code == -1) {
            log.error("赠送流量券MQ：未能识别出手机号码运营商");
        }

        String[] sizes = new String[10];
        if (validMoney > 30000 * 100) {
            if (code == 0) {
                sizes[0] = "11G";
            } else if (code == 1) {
                sizes[0] = "500M";
                Arrays.fill(sizes, 1, 6, "1G");
            } else {
                sizes[0] = "100G";
                Arrays.fill(sizes, 1, 10, "500M");
            }
        } else if (validMoney >= 10000 * 100) {
            if (code == 0) {
                sizes[0] = "6G";
            } else if (code == 1) {
                sizes[0] = "500M";
                Arrays.fill(sizes, 1, 4, "1G");
            } else {
                sizes[0] = "100G";
                Arrays.fill(sizes, 1, 6, "500M");
            }
        } else if (validMoney >= 5000 * 100) {
            if (code == 0) {
                sizes[0] = "2G";
            } else if (code == 1) {
                sizes[2] = "1G";
                Arrays.fill(sizes, 0, 2, "100M");
            } else {
                Arrays.fill(sizes, 0, 6, "500M");
            }
        } else {
            if (code == 2) {
                sizes[0] = "500M";
                sizes[1] = "200M";
            } else {
                sizes[0] = "1G";
            }
        }

        Date nowDate = new Date();
        Date endDate = DateHelper.addDays(nowDate, 7);
        List<String> tempSizes = new ArrayList<>(Arrays.asList(sizes));
        Coupon coupon = new Coupon();
        for (String temp : tempSizes) {
            if (StringUtils.isEmpty(temp)) {
                break;
            }
            coupon.setUserId(tender.getUserId());
            coupon.setStatus(1);
            coupon.setType(0);
            coupon.setStartAt(DateHelper.beginOfDate(nowDate));
            coupon.setEndAt(DateHelper.beginOfDate(endDate));
            coupon.setPhone(phone);
            coupon.setSize(temp);
            coupon.setCreatedAt(nowDate);
            coupon.setUpdatedAt(nowDate);
            couponService.save(coupon);
        }


    }
}
