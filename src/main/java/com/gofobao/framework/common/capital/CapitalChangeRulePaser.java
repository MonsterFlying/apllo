package com.gofobao.framework.common.capital;


import org.apache.commons.beanutils.PropertyUtils;
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
    public static boolean paser(Object target, String rule, long principal, long interest) throws Exception {
        if ((target == null)
                || (StringUtils.isEmpty(rule))
                || (principal < 0)
                || (interest < 0)) {
            logger.error("资金变动规则：参数为空");
            return false;
        }

        String[] rules = rule.split(",");

        for (String subRule : rules) {
            String op;// 操作数
            String opFieldName; // 操作字段名称
            String area  = "all"; // 作用域
            String[] temp;
            long money = 0;
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

            Object value;
            try {
                value = PropertyUtils.getProperty(target, opFieldName);
            } catch (Throwable e) {
                logger.error(String.format("反射获取getter方法失败"), e.getMessage());
                throw new Exception(e) ;
            }

            if( !(value instanceof  Long) ){
                throw new Exception("操作类型不是Long") ;
            }

            switch (op) {
                case "add":
                    money = (long)value + money;
                    break;
                case "sub":
                    money = (long)value - money ;
                    break;
                default:
                    return false;
            }

            if(money < 0){
                throw new Exception("资金表动后数字小于零") ;
            }

            PropertyUtils.setProperty(target, opFieldName, money);
        }

        return true;
    }
}
