package com.gofobao.framework.integral.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.integral.entity.IntegralLog;
import com.gofobao.framework.integral.repository.IntegralLogRepository;
import com.gofobao.framework.integral.service.IntegralLogService;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String, Object> pcIntegralList(VoListIntegralReq integralReq) {
        Map<String,Object>resultMaps= Maps.newHashMap();
        Specification specification= Specifications.<IntegralLog>and()
                .eq("userId",integralReq.getUserId())
                .build();
        Pageable pageable=new PageRequest(integralReq.getPageIndex(),integralReq.getPageSize(),new Sort("id"));
        Page<IntegralLog>integralLogs=integralLogRepository.findAll(specification,pageable);
        Long totalCount=integralLogs.getTotalElements();
        resultMaps.put("totalCount",totalCount);
        resultMaps.put("integralLogs",integralLogs.getContent());
        return resultMaps;
    }
}
