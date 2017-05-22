package com.gofobao.framework.integral.service.impl;

import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.integral.repository.IntegralRepository;
import com.gofobao.framework.integral.service.IntegralService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Zeke on 2017/5/22.
 */
public class IntegralServiceImpl implements IntegralService {

    @Autowired
    private IntegralRepository integralRepository;

    public Integral findByUserId(Long userId) {
        return integralRepository.findOne(userId);
    }
}
