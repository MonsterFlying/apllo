package com.gofobao.framework.finance.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBuyerBiz;
import com.gofobao.framework.finance.constants.FinannceContants;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.finance.vo.response.PlanBuyUserListWarpRes;
import com.gofobao.framework.finance.vo.response.PlanBuyer;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/8/14.
 */
@Service
public class FinancePlanBuyerBizImpl implements FinancePlanBuyerBiz {

    @Autowired
    private FinancePlanBuyerService financePlanBuyerService;


    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<PlanBuyUserListWarpRes> buyUserList(Long id) {
        PlanBuyUserListWarpRes warpRes = VoBaseResp.ok("查询成功", PlanBuyUserListWarpRes.class);

        Specification<FinancePlanBuyer> specification = Specifications.<FinancePlanBuyer>and()
                .eq("planId", id)
                .eq("status", FinannceContants.STATUS_SUCCESS)
                .build();
        List<FinancePlanBuyer> financePlanBuyers = financePlanBuyerService.findList(specification);
        if (CollectionUtils.isEmpty(financePlanBuyers)) {
            return ResponseEntity.ok(warpRes);
        }

        Set<Long> userIds = financePlanBuyers.stream().map(m -> m.getUserId()).collect(Collectors.toSet());
        List<Users> usersList = userService.findByIdIn(new ArrayList<>(userIds));
        Map<Long, Users> usersMap = usersList.stream().collect(Collectors.toMap(Users::getId, Function.identity()));

        List<PlanBuyer> tenderUsers = Lists.newArrayList();
        financePlanBuyers.forEach(p -> {
            Users users = usersMap.get(p.getUserId());
            PlanBuyer buyer = new PlanBuyer();
            buyer.setDate(DateHelper.dateToString(p.getCreatedAt()));
            buyer.setValidMoney(StringHelper.formatMon(p.getValidMoney() / 100D));
            buyer.setSource(p.getSource());
            buyer.setUserName(StringUtils.isEmpty(users.getUsername()) ?
                    UserHelper.hideChar(users.getPhone(), UserHelper.PHONE_NUM) :
                    UserHelper.hideChar(users.getUsername(), UserHelper.USERNAME_NUM));
            tenderUsers.add(buyer);
        });
        warpRes.setTenderUsers(tenderUsers);
        return ResponseEntity.ok(warpRes);
    }

}
