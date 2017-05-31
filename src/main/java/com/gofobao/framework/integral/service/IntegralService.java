package com.gofobao.framework.integral.service;

import com.gofobao.framework.integral.entity.Integral;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface IntegralService {

    Integral findByUserId(Long userId);

    Integral findByUserIdLock(Long userId);

    boolean insert(Integral integral);

    boolean updateById(Integral integral);
}
