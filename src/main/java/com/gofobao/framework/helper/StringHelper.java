package com.gofobao.framework.helper;

import static com.google.common.base.Preconditions.*;

import org.springframework.util.ObjectUtils;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Max on 2017/3/6.
 */
public class StringHelper {

    public static String toString(Object obj) {
        return ObjectUtils.isEmpty(obj) ? "" : obj.toString();
    }

    public static void main(String[] args) {

        try {
            System.out.println(formatDouble(123545, 100, false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 格式化金额
     *
     * @param number 单位分
     * @param symbol 是否有千分符
     * @return
     */
    public static String formatDouble(double number, boolean symbol) {
        String va = "0.0";
        if (number > 0.001) {
            String fomat = null;
            if (symbol) {
                fomat = "#,##0.00";
            } else {
                fomat = "###0.00";
            }
            DecimalFormat df = new DecimalFormat(fomat);
            va = df.format(number);
        }
        return va;
    }

    /**
     * 格式化金额
     *
     * @param number 单位分
     * @param symbol 是否添加千分符
     * @return
     */
    public static String formatDouble(double number, double divisor, boolean symbol) {
        return formatDouble(NumberHelper.toDouble(number) / NumberHelper.toDouble(divisor), symbol);
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
            key = String.format("%s%s%s", leftPlaceholder, key, rightPlaceholder);
            template = template.replace(key, value);
        }

        return template;
    }


    /**
     * 获取Map的待签名字符串
     *
     * @param map
     * @return
     */
    public static String mergeMap(Map<String, String> map) {
        checkNotNull(map, "");
        Map<String, String> reqMap = new TreeMap<String, String>(map);

        StringBuffer buff = new StringBuffer();

        Iterator<Map.Entry<String, String>> iter = reqMap.entrySet().iterator();
        Map.Entry<String, String> entry;
        while (iter.hasNext()) {
            entry = iter.next();
            if (!"sign".equals(entry.getKey())) {
                if (entry.getValue() == null) {
                    entry.setValue("");
                    buff.append("");
                } else {
                    buff.append(String.valueOf(entry.getValue()));
                }
            }
        }

        return buff.toString();
    }

}
