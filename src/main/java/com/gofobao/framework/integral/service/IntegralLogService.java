package com.gofobao.framework.integral.service;

import com.gofobao.framework.integral.entity.IntegralLog;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface IntegralLogService {
    List<IntegralLog> findListByUserId(Long userId, Pageable pageable);

    IntegralLog insert(IntegralLog integralLog);

    boolean updateById(IntegralLog integralLog);


    Map<String,Object>pcIntegralList(VoListIntegralReq integralReq);
}
