package com.gofobao.framework.api.helper;

import com.gofobao.framework.helper.DateHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Component
public class JixinTxDateHelper {

    @Value("${jixin.query-time-init}")
    String queryTimeInit;


    /**
     * 获取系统查询时间
     *
     * @return
     */
    public Date getTxDate() {
        Date date ;
        if(StringUtils.isEmpty(queryTimeInit)){
            date = new Date() ;
        }else{
            return DateHelper.stringToDate(queryTimeInit, DateHelper.DATE_FORMAT_YMD_NUM);
        }
        return date;
    }


    /**
     * 获取查询时间
     *
     * @return
     */
    public String getTxDateStr() {
        Date txDate = getTxDate();
        return DateHelper.dateToString(txDate, DateHelper.DATE_FORMAT_YMD_NUM);
    }

    /**
     * 减去多少天
     *
     * @param day 减去天数
     * @return
     */
    public String getSubDateStr(int day) {
        Date txDate = getSubDate(day);
        return DateHelper.dateToString(txDate, DateHelper.DATE_FORMAT_YMD_NUM);
    }


    /**
     * 减去多少天
     *
     * @param day 减去天数
     * @return
     */
    public Date getSubDate(int day) {
        Date txDate = getTxDate();
        return DateHelper.subDays(txDate, day);
    }

}
