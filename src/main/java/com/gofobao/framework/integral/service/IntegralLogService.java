package com.gofobao.framework.integral.service;

import com.gofobao.framework.integral.entity.IntegralLog;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface IntegralLogService {
    List<IntegralLog> findByUserId(Long userId,int pageIndex,int pageSize);

    boolean insert(IntegralLog integralLog);

    boolean update(IntegralLog integralLog);
}
