package com.gofobao.framework.currency.service.impl;

import com.gofobao.framework.currency.entity.CurrencyLog;
import com.gofobao.framework.currency.repository.CurrencyLogRepository;
import com.gofobao.framework.currency.service.CurrencyLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Created by Zeke on 2017/5/23.
 */
@Service
public class CurrencyLogServiceImpl implements CurrencyLogService {

    @Autowired
    private CurrencyLogRepository currencyLogRepository;

    public List<CurrencyLog> findListByUserId(Long userId, Pageable pageable){
        return currencyLogRepository.findByUserId(userId);
    }

    public boolean insert(CurrencyLog currencyLog){
        if (ObjectUtils.isEmpty(currencyLog)){
            return false;
        }
        currencyLog.setId(null);
        return !ObjectUtils.isEmpty(currencyLogRepository.save(currencyLog));
    }

    public boolean updateById(CurrencyLog currencyLog){
        if (ObjectUtils.isEmpty(currencyLog) || ObjectUtils.isEmpty(currencyLog.getId())){
            return false;
        }
        return !ObjectUtils.isEmpty(currencyLogRepository.save(currencyLog));
    }
}
