package com.gofobao.framework.listener.providers;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.biz.IncrStatisticBiz;
import com.gofobao.framework.system.biz.NoticesBiz;
import com.gofobao.framework.system.entity.IncrStatistic;
import com.gofobao.framework.system.entity.Notices;
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
    UserThirdBiz userThirdBiz ;

    @Autowired
    CapitalChangeHelper capitalChangeHelper ;

    @Autowired
    IncrStatisticBiz incrStatisticBiz ;

    @Autowired
    RechargeDetailLogService rechargeDetailLogService ;

    @Autowired
    NoticesBiz noticesBiz ;

    @Autowired
    UserThirdAccountService userThirdAccountService  ;

    @Autowired
    JixinManager jixinManager ;


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


    /**
     * 充值成功
     * @param msg
     * @return
     */
    public boolean recharge(Map<String, String> msg) {
        Long rechargeId = Long.parseLong(msg.get(MqConfig.MSG_ID));
        if(ObjectUtils.isEmpty(rechargeId)){
            log.error("充值MQ回调参数为空");
            return false ;
        }


        RechargeDetailLog rechargeDetailLog = rechargeDetailLogService.findById(rechargeId) ;
        if(ObjectUtils.isEmpty(rechargeDetailLog)){
            log.error("查无充值记录");
            return false ;
        }

        // 充值发送通知
        String name = String.format("充值%s元已成功", StringHelper.formatDouble(rechargeDetailLog.getMoney() / 100D, true));
        String content = String.format("您已经于%s成功充值%s元", DateHelper.dateToString(rechargeDetailLog.getCreateTime()), StringHelper.formatDouble(rechargeDetailLog.getMoney() / 100D, true));
        Notices notices = new Notices();
        notices.setRead(false);
        notices.setCreatedAt(new Date());
        notices.setUserId(rechargeDetailLog.getUserId());
        notices.setFromUserId(0L);
        notices.setContent(content);
        notices.setName(name);
        notices.setType("system");
        noticesBiz.save(notices) ;
        return true;
    }


    /**
     *  用户登录事件触发
     * @param msg
     * @return
     */
    public boolean userLogin(Map<String, String> msg) {
        Long userId = Long.parseLong(msg.get(MqConfig.MSG_USER_ID)) ;
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
            return true ;
        }

        //===================
        // 同步签约状态
        //===================
        userThirdBiz.synCreditQuth(userThirdAccount) ;

        //=================
        // 同步提现
        //=================


        //=================
        // 同步充值
        //=================


        return true;
    }
}
