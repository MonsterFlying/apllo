package com.gofobao.framework.integral.service;

import com.gofobao.framework.integral.entity.IntegralLog;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface IntegralLogService {
    List<IntegralLog> findListByUserId(Long userId, Pageable pageable);

    boolean insert(IntegralLog integralLog);

    boolean updateById(IntegralLog integralLog);
}
