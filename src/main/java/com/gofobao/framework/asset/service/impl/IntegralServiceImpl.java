package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.Integral;
import com.gofobao.framework.asset.repository.IntegralRepository;
import com.gofobao.framework.asset.service.IntegralService;
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
