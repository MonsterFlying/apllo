package com.gofobao.framework.listener.providers;

import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.system.biz.IncrStatisticBiz;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.entity.IncrStatistic;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.entity.Statistic;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Map;

/**
 * Created by Max on 17/6/1.
 */
@Component
@Slf4j
public class UserActiveProvider {
    @Autowired
    UserService userService;

    @Autowired
    CapitalChangeHelper capitalChangeHelper ;

    @Autowired
    IncrStatisticBiz incrStatisticBiz ;


    /**
     * 注册活动
     * @param body
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean  registerActive( Map<String, String> body) throws Exception{
        Long userId = Long.parseLong(body.get(MqConfig.MSG_USER_ID));
        // 赠送用户体验金
        awardVirtualMoney(userId, 1000);
        // 增加统计
        IncrStatistic incrStatistic = new IncrStatistic() ;
        incrStatistic.setRegisterCount(1);
        incrStatistic.setRegisterTotalCount(1);
        incrStatisticBiz.caculate(incrStatistic) ;
        return true;
    }

    /**
     * 赠送体验金
     * @param userId
     * @param money
     * @throws Exception
     */
    private void awardVirtualMoney(Long userId, Integer money ) throws Exception{
        log.info(String.format("award VirtualMoney: %s", userId));
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.AwardVirtualMoney);
        entity.setUserId(userId);
        entity.setMoney(money * 100);
        entity.setRemark("赠送体验金");
        capitalChangeHelper.capitalChange(entity);
        log.info("award virtualMoney success");
    }

}
