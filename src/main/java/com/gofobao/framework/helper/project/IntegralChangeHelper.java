package com.gofobao.framework.helper.project;

import com.gofobao.framework.common.capital.CapitalChangeRulePaser;
import com.gofobao.framework.common.integral.IntegralChangeConfig;
import com.gofobao.framework.common.integral.IntegralChangeEntity;
import com.gofobao.framework.common.integral.IntegralChangeEnum;
import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.integral.entity.IntegralLog;
import com.gofobao.framework.integral.service.IntegralLogService;
import com.gofobao.framework.integral.service.IntegralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/6/7.
 */
@Component
public class IntegralChangeHelper {

    @Autowired
    private IntegralService integralService;
    @Autowired
    private IntegralLogService integralLogService;

    @Transactional(rollbackFor = Exception.class)
    public boolean integralChange(IntegralChangeEntity entity) throws Exception{
        Long userId = entity.getUserId();
        Integer value = entity.getValue();
        IntegralChangeEnum type = entity.getType();
        if (ObjectUtils.isEmpty(userId) || ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(type)){
            return false;
        }

        Integral integral = integralService.findByUserIdLock(userId);

        IntegralLog integralLog = new IntegralLog();
        integralLog.setUserId(userId);
        integralLog.setType(type.getValue());
        integralLog.setUseIntegral(integral.getUseIntegral());
        integralLog.setNoUseIntegral(integral.getNoUseIntegral());
        integralLog.setValue(value);
        integralLog.setCreatedAt(new Date());

        IntegralChangeConfig config = findIntegralChangeConfig(type);
        boolean flag = CapitalChangeRulePaser.paser(integralLog,config.getIntegralChangeRule(),value,0);
        if (!flag){
            throw new Exception("获取积分变动配置失败!");
        }

        integralLog = integralLogService.insert(integralLog);

        integral.setUseIntegral(integralLog.getUseIntegral());
        integral.setNoUseIntegral(integralLog.getNoUseIntegral());
        integralService.updateById(integral);

        return true;
    }

    /**
     * 查找资金变动记录表
     *
     * @param integralChangeEnum
     * @return
     */
    private IntegralChangeConfig findIntegralChangeConfig(IntegralChangeEnum integralChangeEnum) {
        List<IntegralChangeConfig> IntegralChangeList = IntegralChangeConfig.integralChangeList;
        for (IntegralChangeConfig config : IntegralChangeList) {
            if (config.getType()!=null&&config.getType().equals(integralChangeEnum)) {
                return config;
            }
        }
        return null;
    }
}
