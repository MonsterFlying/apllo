package com.gofobao.framework.integral.service;

import com.gofobao.framework.integral.entity.Integral;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface IntegralService {

    Integral findByUserId(Long userId);
}
