package com.gofobao.framework.integral.service.impl;

import com.gofobao.framework.integral.entity.IntegralLog;
import com.gofobao.framework.integral.repository.IntegralLogRepository;
import com.gofobao.framework.integral.service.IntegralLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
public class IntegralLogServiceImpl implements IntegralLogService {

    @Autowired
    private IntegralLogRepository integralLogRepository;

    public List<IntegralLog> findListByUserId(Long userId, Pageable pageable) {
        return integralLogRepository.findByUserId(userId, pageable);
    }

    public IntegralLog insert(IntegralLog integralLog){
        if (ObjectUtils.isEmpty(integralLog)){
            return null;
        }
        integralLog.setId(null);
        return integralLogRepository.save(integralLog);
    }

    public boolean updateById(IntegralLog integralLog){
        if (ObjectUtils.isEmpty(integralLog) || ObjectUtils.isEmpty(integralLog.getId())){
            return false;
        }
        return !ObjectUtils.isEmpty(integralLogRepository.save(integralLog));
    }
}
