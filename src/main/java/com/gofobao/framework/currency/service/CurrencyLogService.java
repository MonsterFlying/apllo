package com.gofobao.framework.currency.service;

import com.gofobao.framework.currency.entity.CurrencyLog;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by Zeke on 2017/5/23.
 */
public interface CurrencyLogService {

    List<CurrencyLog> findListByUserId(Long userId, Pageable pageable);

    CurrencyLog insert(CurrencyLog currencyLog);

    boolean updateById(CurrencyLog currencyLog);
}
