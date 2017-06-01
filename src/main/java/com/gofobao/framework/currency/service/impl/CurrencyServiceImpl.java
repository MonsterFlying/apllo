package com.gofobao.framework.currency.service.impl;

import com.gofobao.framework.currency.entity.Currency;
import com.gofobao.framework.currency.repository.CurrencyRepository;
import com.gofobao.framework.currency.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/5/23.
 */
@Service
public class CurrencyServiceImpl implements CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    public Currency findByUserIdLock(Long userId){
        return currencyRepository.findByUserId(userId);
    }

    public Currency findByUserId(Long userId){
        return currencyRepository.findOne(userId);
    }

    public Currency save(Currency currency){ return currencyRepository.save(currency); }

    public Currency updateById(Currency currency){ return currencyRepository.save(currency); }
}
