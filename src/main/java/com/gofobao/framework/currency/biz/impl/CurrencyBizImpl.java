package com.gofobao.framework.currency.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.currency.biz.CurrencyBiz;
import com.gofobao.framework.currency.entity.Currency;
import com.gofobao.framework.currency.entity.CurrencyLog;
import com.gofobao.framework.currency.service.CurrencyLogService;
import com.gofobao.framework.currency.service.CurrencyService;
import com.gofobao.framework.currency.vo.request.VoConvertCurrencyReq;
import com.gofobao.framework.currency.vo.request.VoListCurrencyReq;
import com.gofobao.framework.currency.vo.response.VoCurrency;
import com.gofobao.framework.currency.vo.response.VoListCurrencyResp;
import com.gofobao.framework.helper.DateHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;


/**
 * Created by Zeke on 2017/5/23.
 */
@Service
public class CurrencyBizImpl implements CurrencyBiz {

    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private CurrencyLogService currencyLogService;

    private static Map<String, String> currencyTypeMap = new HashMap<>();

    static {
        currencyTypeMap.put("daily_settlement", "每日结算");
        currencyTypeMap.put("month_settlement", "每月结算");
        currencyTypeMap.put("convert", "兑换");
    }

    public ResponseEntity<VoBaseResp> list(VoListCurrencyReq voListCurrencyReq) {
        int pageSize = voListCurrencyReq.getPageSize();
        int pageIndex = voListCurrencyReq.getPageIndex();
        Long userId = voListCurrencyReq.getUserId();


        Currency currency = currencyService.findByUserId(userId);
        if (currency == null) {
            return null;
        }

        //分页和排序
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = new PageRequest(pageIndex, pageSize, sort);

        List<CurrencyLog> currencyLogs = currencyLogService.findListByUserId(userId, pageable);
        if (CollectionUtils.isEmpty(currencyLogs)) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "获取广富币列表失败!"));
        }

        VoListCurrencyResp voListCurrencyResp = VoBaseResp.ok("查询成功!", VoListCurrencyResp.class);
        voListCurrencyResp.setAvailableCurrency(currency.getUseCurrency());
        voListCurrencyResp.setInvalidCurrency(currency.getNoUseCurrency());
        voListCurrencyResp.setTotalCurrency(currency.getNoUseCurrency() + currency.getUseCurrency());

        List<VoCurrency> currencyList = new ArrayList<>();
        Optional<List<CurrencyLog>> objCurrencys = Optional.ofNullable(currencyLogs);
        objCurrencys.ifPresent(p -> p.forEach(currencyLog -> {
            VoCurrency voCurrency = new VoCurrency();
            voCurrency.setTotalCurrency(currencyLog.getUseCurrency());
            voCurrency.setDate(DateHelper.dateToStringYearMouthDay(currencyLog.getCreatedAt()));
            voCurrency.setCurrency("convert".equalsIgnoreCase(currencyLog.getType()) ? String.format("-%s", currencyLog.getValue()) : String.format("+%s", currencyLog.getValue()));
            voCurrency.setType(currencyLog.getType());
            voCurrency.setTypeName(findCurrencyMap(currencyLog.getType()));
            currencyList.add(voCurrency);
        }));

        voListCurrencyResp.setVoCurrencyList(currencyList);
        return ResponseEntity.ok(voListCurrencyResp);
    }

    /**
     * 兑换广福币
     *
     * @return
     */
    public ResponseEntity<Integer> convert(VoConvertCurrencyReq voConvertCurrencyReq) {
        return null;
    }


    /**
     * 查找积分类型名称
     *
     * @param type
     * @return
     */
    private String findCurrencyMap(String type) {
        String typeName = currencyTypeMap.get(type);
        return StringUtils.isEmpty(typeName) ? "其他" : typeName;
    }
}
