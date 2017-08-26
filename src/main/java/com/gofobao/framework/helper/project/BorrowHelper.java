package com.gofobao.framework.helper.project;

import com.gofobao.framework.helper.MathHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Max on 2017/3/27.
 */
public class BorrowHelper {


    /**
     * 计算RepayFashions
     *
     * @param repayFashions borrow表的repayFashion字段
     * @return
     */
    public static String countRepayFashions(Integer... repayFashions) {
        StringBuffer condition = new StringBuffer();//拼接条件
        StringBuffer binary;
        String tempStr;
        char[] binaryChar;
        for (int i = 1, len = MathHelper.pow(2, 3); i < len; i++) {
            binary = new StringBuffer();
            binaryChar = new char[3];
            tempStr = Integer.toBinaryString(i) + "";
            for (int j = 0; j < 3 - tempStr.length(); j++) {
                binary.append("0");
            }
            binary.append(tempStr).reverse();

            Set<Integer> num = new HashSet<>();
            binary.getChars(0, binary.length(), binaryChar, 0);
            for (Integer repayFashion : repayFashions) {
                if (binaryChar[repayFashion] == '1') {
                    num.add(i);
                }
            }

            for (Integer tempNum : num) {
                condition.append(tempNum).append(",");
            }
        }
        return condition.substring(0, condition.length() - 1);
    }

    /**
     * 获取债权转让费率
     * @param leftOrder
     * @return
     */
    public static double getTransferFeeRate(int leftOrder){
        return Math.min(0.004 + 0.0008 * (leftOrder - 1), 0.0128); // 获取债权转让费用
    }
}
