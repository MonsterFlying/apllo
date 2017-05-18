package com.gofobao.framework.helper;

import org.springframework.util.ObjectUtils;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Max on 2017/3/6.
 */
public class StringHelper {

    public static String toString(Object obj) {
        return ObjectUtils.isEmpty(obj) ? "" : obj.toString();
    }


    /**
     * 保留两位小数
     *
     * @param number
     * @return
     */
    public static String formatDouble(double number) {
        return formatDouble(number,2);
    }

    /**
     * 保留两位小数
     *
     * @param number
     * @param retain 保留小数位
     * @return
     */
    public static String formatDouble(double number,int retain) {
        StringBuffer format = new StringBuffer("#");
        if (retain>0){
            format.append(".");
        }
        for (int i = 0;i<retain;i++){
            format.append("0");
        }
        DecimalFormat df = new DecimalFormat(format.toString());
        return df.format(number);
    }

    public static void main(String[] args) {

        try {
            System.out.println(formatMoney(1.0,100.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 格式化金额
     * @param number 单位分
     * @return
     */
    public static  String formatMoney(double number){
        String va = "0.0";
        if (number > 0.001) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            va = df.format(number);
        }
        return va;
    }

    /**
     * 格式化金额
     * @param number 单位分
     * @return
     */
    public static  String formatMoney(double number,double divisor){
        return formatMoney(number/divisor);
    }

    /**
     * 格式化金额
     * @param number 单位分
     * @return
     */
    public static  String formatMoney(String number,double divisor){
        return formatMoney(NumberHelper.toDouble(number),new Double(divisor).intValue());
    }


    /**
     * 替换模板
     *
     * @param template 魔板
     * @param params   替换参数
     * @return 替换后模板
     */
    public static String replateTemplace(String template, String leftPlaceholder, Map<String, String> params, String rightPlaceholder) {
        Iterator<String> iterator = params.keySet().iterator();
        String key = null;
        String value = null;

        while (iterator.hasNext()) {
            key = iterator.next();
            value = params.get(key);
            key = String.format("%s%s%s",leftPlaceholder, key,rightPlaceholder);
            template = template.replace(key, value);
        }

        return template;
    }

}
