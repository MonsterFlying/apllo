package com.gofobao.framework.award.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.award.contants.CouponContants;
import com.gofobao.framework.award.entity.Coupon;
import com.gofobao.framework.award.repository.CouponRepository;
import com.gofobao.framework.award.service.CouponService;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.award.vo.response.CouponRes;
import com.gofobao.framework.helper.DateHelper;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * Created by admin on 2017/6/7.
 */
@Component
public class CouponServiceImpl implements CouponService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private CouponRepository couponRepository;


    @Override
    public List<CouponRes> list(VoCouponReq couponReq) {

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT coupon FROM Coupon coupon WHERE coupon.userId =:userId");
        String condition = "";
        if (couponReq.getStatus() == CouponContants.STATUS_YES) {
            condition = " and (status=" + CouponContants.VALID + ")";
        } else if (couponReq.getStatus() == CouponContants.STATUS_NO) {
            condition = " and (status=" + CouponContants.LOCK +
                    " or status=" + CouponContants.LOCK +
                    " or status=" + CouponContants.USED +
                    " or status=" + CouponContants.FAILURE + ")";
        } else {
            return Collections.EMPTY_LIST;
        }
        sql.append(condition);
        TypedQuery<Coupon> query = entityManager.createQuery(sql.toString(), Coupon.class)
                .setParameter("userId", couponReq.getUserId())
                .setFirstResult(couponReq.getPageIndex() * couponReq.getPageSize())
                .setMaxResults(couponReq.getPageSize());
        List<Coupon> couponList = query.getResultList();
        if (CollectionUtils.isEmpty(couponList)) {
            return Collections.EMPTY_LIST;
        }
        return commonHandle(couponList);

    }

    @Override
    public Map<String, Object> pcList(VoCouponReq couponReq) {
        Map<String, Object> resultMaps = Maps.newHashMap();
        Specification specification = Specifications.<Coupon>and()
                .eq("userId", couponReq.getUserId())
                .eq("status", couponReq.getStatus())
                .build();
        Page<Coupon> couponList = couponRepository.findAll(specification,
                new PageRequest(couponReq.getPageIndex(),
                        couponReq.getPageSize(),
                        new Sort("id")));
        Long totalCount = couponList.getTotalElements();
        resultMaps.put("totalCount", totalCount);
        List<Coupon> coupons = couponList.getContent();
        if (CollectionUtils.isEmpty(coupons)) {
            resultMaps.put("couponResList", new ArrayList<>(0));
        } else {
            List<CouponRes> couponResList = commonHandle(couponList.getContent());
            resultMaps.put("couponResList", couponResList);
        }
        return resultMaps;
    }


    /**
     * 公共处理
     *
     * @param couponList
     * @return
     */
    public List<CouponRes> commonHandle(List<Coupon> couponList) {
        List<CouponRes> resList = new ArrayList<>(couponList.size());
        couponList.stream().forEach(p -> {
            CouponRes couponRes = new CouponRes();
            couponRes.setId(p.getId());
            couponRes.setPhone(p.getPhone());
            couponRes.setSizeStr(p.getSize());
            couponRes.setExpiryDate(DateHelper.dateToString(p.getStartAt(), DateHelper.DATE_FORMAT_YMD) + "~" + DateHelper.dateToString(p.getEndAt(), DateHelper.DATE_FORMAT_YMD));
            resList.add(couponRes);
        });
        return Optional.ofNullable(resList).orElse(Collections.EMPTY_LIST);
    }


    @Override
    public List<Coupon> takeFlow(Long userId, Long couponId) {

        Specification specification = Specifications.<Coupon>and()
                .eq("userId", userId)
                .eq("id", couponId)
                .build();
        List<Coupon> couponList = couponRepository.findAll(specification);
        if (ObjectUtils.isEmpty(couponList)) {
            return Collections.EMPTY_LIST;
        }

        return couponList;
    }


    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

}
