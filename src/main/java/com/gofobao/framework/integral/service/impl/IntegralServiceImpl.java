package com.gofobao.framework.integral.service.impl;

import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.integral.repository.IntegralRepository;
import com.gofobao.framework.integral.service.IntegralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
public class IntegralServiceImpl implements IntegralService {

    @Autowired
    private IntegralRepository integralRepository;

    public Integral findByUserIdLock(Long userId){
        return integralRepository.findByUserId(userId);
    }

    public Integral findByUserId(Long userId) {
        return integralRepository.findOne(userId);
    }

    public Integral save(Integral integral){
        return integralRepository.save(integral);
    }

    public Integral updateById(Integral integral){
        return integralRepository.save(integral);
    }
}
