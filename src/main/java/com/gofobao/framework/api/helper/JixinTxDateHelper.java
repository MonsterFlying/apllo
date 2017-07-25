package com.gofobao.framework.api.helper;

import com.gofobao.framework.helper.DateHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JixinTxDateHelper {

    @Value("${jixin.time-interval}")
    int timeInterval;


    /**
     * 获取系统查询时间
     *
     * @return
     */
    public Date getTxDate() {
        Date nowDate = new Date();
        return DateHelper.subDays(nowDate, timeInterval);
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
