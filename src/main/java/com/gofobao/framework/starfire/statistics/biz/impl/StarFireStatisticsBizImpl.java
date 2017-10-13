package com.gofobao.framework.starfire.statistics.biz.impl;

import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.starfire.common.request.BaseRequest;
import com.gofobao.framework.starfire.common.response.CodeTypeConstant;
import com.gofobao.framework.starfire.common.response.ResultCodeEnum;
import com.gofobao.framework.starfire.statistics.biz.StarFireStatisticsBiz;
import com.gofobao.framework.starfire.statistics.vo.request.StatisticsQuery;
import com.gofobao.framework.starfire.statistics.vo.response.StatisticsDataRes;
import com.gofobao.framework.starfire.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by master on 2017/9/29.
 */
@Service
@Slf4j
public class StarFireStatisticsBizImpl implements StarFireStatisticsBiz {

    @Autowired
    private BaseRequest baseRequest;

    @Value("${starfire.key}")
    private  String key;

    @Value("${starfire.initVector}")
    private  String initVector;

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public StatisticsDataRes query(StatisticsQuery statisticsQuery) {
        //封装验证参数
        baseRequest.setT_code(statisticsQuery.getT_code());
        baseRequest.setC_code(statisticsQuery.getC_code());
        baseRequest.setSerial_num(statisticsQuery.getSerial_num());
        baseRequest.setSign(statisticsQuery.getSign());

        //封装返回参数
        StatisticsDataRes statisticsDataRes = new StatisticsDataRes();
        statisticsDataRes.setSerial_num(statisticsQuery.getSerial_num());
        String refDate = statisticsQuery.getRefdate();
        //验签
        if (!SignUtil.checkSign(baseRequest, key, initVector)
                || StringUtils.isEmpty(refDate)) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
            statisticsDataRes.setResult(code);
            return statisticsDataRes;
        }
        Query query = entityManager.createNativeQuery("SELECT " +
                "COUNT(DISTINCT tender.user_id) AS userCount, " +
                "SUM(tender.valid_money) AS moneySum, " +
                "COUNT(DISTINCT tender.borrow_id) AS borrowTotal " +
                "FROM " +
                "gfb_borrow_tender AS tender " +
                "WHERE " +
                "tender.status=1 " +
                "AND " +
                "tender.created_at<'" + refDate + "'");
        List rows = query.getResultList();
        Integer userCount = 0;
        Long moneySum = 0L;
        Integer borrowTotal = 0;
        for (Object row : rows) {
            Object[] cells = (Object[]) row;
            userCount = Integer.valueOf(cells[0].toString());
            moneySum = Long.valueOf(cells[1].toString());
            borrowTotal = Integer.valueOf(cells[2].toString());
        }
        List<StatisticsDataRes.Records> records = new ArrayList<>(1);
        StatisticsDataRes.Records record = statisticsDataRes.new Records();
        record.setLendCount(userCount);
        record.setTotalBidMoney(StringHelper.formatDouble(moneySum / 100D, false));
        record.setBorrowCount(borrowTotal);
        Query repaymentQuery=entityManager.createNativeQuery(
                "SELECT sum(repayment.repay_money) FROM gfb_borrow_repayment repayment " +
                " JOIN  gfb_borrow borrow " +
                " ON " +
                " repayment.borrow_id=borrow.id " +
                " WHERE" +
                " repayment.status=0 " +
                " AND " +
                " borrow.recheck_at<'"+refDate+"'");
        List resultList=repaymentQuery.getResultList();
        record.setTotalBackMoney(StringHelper.formatDouble(Long.valueOf(resultList.get(0).toString()),100D,false));
        record.setRefdate(refDate);
        records.add(record);
        statisticsDataRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
        statisticsDataRes.setRecords(records);
        return statisticsDataRes;
    }
}
