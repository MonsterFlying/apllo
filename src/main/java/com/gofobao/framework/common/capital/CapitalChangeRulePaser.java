package com.gofobao.framework.common.capital;


import com.gofobao.framework.helper.MethodInvokerHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 资金规则解析
 * Created by Max on 17/3/10.
 */
public class CapitalChangeRulePaser {
    public static Logger logger = LoggerFactory.getLogger(CapitalChangeRulePaser.class);

    /**
     * 解析资金变动规则
     *
     * @param target    备操作对象
     * @param rule      解析规则
     * @param principal 本金
     * @param interest  利息
     * @return 解析是否成功
     */
    public static boolean paser(Object target, String rule, int principal, int interest) {
        if ((target == null)
                || (StringUtils.isEmpty(rule))
                || (principal < 0)
                || (interest < 0)) {
            logger.error("资金变动规则：参数为空");
            return false;
        }

        String[] rules = rule.split(",");
        String op = null;// 操作数
        String opFieldName = null; // 操作字段名称
        String area = null;
        String[] temp = null;
        Integer money = 0;
        for (String subRule : rules) {
            area = "all"; // 作用域
            if (StringUtils.isEmpty(subRule)) {
                logger.error(String.format("资金变动规则：%s为空", subRule));
                return false;
            }

            temp = subRule.split("@");
            if (temp.length != 2) {
                logger.error(String.format("资金变动规则：%s格式错误", subRule));
                return false;
            }

            op = temp[0];
            if (temp[1].contains("#")) {
                temp = temp[1].split("#");
                if (temp.length != 2) {
                    logger.error(String.format("资金变动规则：%s格式错误", temp[1]));
                    return false;
                }

                opFieldName = temp[0];
                area = temp[1];
            } else {
                opFieldName = temp[1];
            }

            //处理钱的问题
            switch (area) {
                case "principal":
                    money = principal;
                    break;
                case "interest":
                    money = interest;
                    break;

                case "all":
                    money = interest + principal;
                    break;
                default:
                    logger.error(String.format("资金变动规则：%s格式错误", area));
                    return false;
            }

            Object value = null;
            try {
                value = MethodInvokerHelper.createGetter(target.getClass(), false, opFieldName).invoke(target);
            } catch (Exception e) {
                logger.error(String.format("反射获取getter方法失败"), e.getMessage());
                return false;
            }
            switch (op) {
                case "add":
                    money = NumberHelper.toInt(StringHelper.toString(value)) + money;
                    break;
                case "sub":
                    money = NumberHelper.toInt(StringHelper.toString(value)) - money;
                    break;
                default:
                    return false;
            }


            try {
                if ((value.getClass() == Long.class)) {
                    long longMoney = money.longValue();
                    MethodInvokerHelper.createSetter(target.getClass(), opFieldName, false, value.getClass()).invoke(target, longMoney);
                } else if (value.getClass() == Integer.class) {
                    MethodInvokerHelper.createSetter(target.getClass(), opFieldName, false, value.getClass()).invoke(target, money);
                } else if (value.getClass() == Short.class) {
                    short shortMoney = money.shortValue();
                    MethodInvokerHelper.createSetter(target.getClass(), opFieldName, false, value.getClass()).invoke(target, shortMoney);
                }

            } catch (Exception e) {
                logger.error(String.format("反射获取setter方法失败"), e.getMessage());
                return false;
            }

        }

        return true;
    }
}
