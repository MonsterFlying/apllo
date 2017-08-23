package com.gofobao.framework.migrate;

import org.apache.commons.lang3.StringUtils;

public class FormatHelper {

    public static String appendByTail(String valueStr, int length) throws Exception {
        valueStr = StringUtils.trim(valueStr);
        int gbk_length = valueStr.getBytes("gbk").length;
        StringBuffer value = new StringBuffer(valueStr);
        int real_length = valueStr.length();
        if (gbk_length > valueStr.length()) {
            real_length = gbk_length;
        }

        if (real_length < length) {
            // 不相等长高度
            int leafLen = length - real_length;
            for (int i = 0; i < leafLen; i++) {
                value.append(" ");
            }
            return value.toString();
        } else if (gbk_length == length) {
            return valueStr;
        } else {
            throw new Exception("写入范围超标");
        }

    }


    public static String getStr(byte[] datas, int begin, int end) throws Exception {
        if (datas.length < end) {
            throw new Exception("越界了");
        }

        byte[] temp = new byte[end - begin];
        for (int i = 0, len = datas.length; i < len; i++) {
            if(i < begin || i >= end){
                continue;
            }
            temp[i - begin] = datas[i] ;
        }
        String str = new String(temp, "gbk") ;
        return StringUtils.trim(str) ;
    }

    public static String appendByPre(String valueStr, int length) throws Exception {
        valueStr = StringUtils.trim(valueStr);
        int gbk_length = valueStr.getBytes("gbk").length;
        StringBuffer value = new StringBuffer();
        int real_length = valueStr.length();
        if (gbk_length > valueStr.length()) {
            real_length = gbk_length;
        }

        if (real_length < length) {
            // 不相等长高度
            int leafLen = length - real_length;
            for (int i = 0; i < leafLen; i++) {
                value.append(" ");
            }
            value.append(valueStr);
            return value.toString();
        } else if (gbk_length == length) {
            return valueStr;
        } else {
            throw new Exception("写入范围超标");
        }
    }
}
