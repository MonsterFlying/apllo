package com.gofobao.framework.common.assets;


import cn.jiguang.common.utils.Preconditions;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.util.StringUtils;

public class AssetChangeRuleParse {

    /**
     * 解析参数
     *
     * @param target
     * @param rule
     * @param principal
     * @param interest
     * @throws Exception
     */
    public static void parse(Object target, String rule, long principal, long interest) throws Exception {
        Preconditions.checkNotNull(target, "AssetChangeRuleParse.parse target is null ");
        Preconditions.checkNotNull(rule, "AssetChangeRuleParse.parse rule is null ");
        Preconditions.checkArgument(principal >= 0, "AssetChangeRuleParse.parse principal < 0 ");
        Preconditions.checkArgument(interest >= 0, "AssetChangeRuleParse.parse interest < 0 ");
        String[] ruleArr = rule.split(",");
        String op = null, opFieldNume = null;
        String[] temp = null;


        for (String subRule : ruleArr) {
            int area = 0;
            Preconditions.checkNotNull(!StringUtils.isEmpty(subRule), "AssetChangeRuleParse.parse subRule is empty");
            temp = subRule.split("@");
            Preconditions.checkArgument(temp.length == 2, "AssetChangeRuleParse.parse temp length not equals 2");
            op = temp[0];
            if (temp[1].contains("#")) {
                temp = temp[1].split("#");
                Preconditions.checkArgument(temp.length == 2,
                        "AssetChangeRuleParse.parse temp[1] length not equals 2");
                opFieldNume = temp[0];
                if ("principal".equals(temp[1])) {
                    area = 1;
                } else if ("interest".equals(temp[1])) {
                    area = 2;
                } else {
                    area = 3;
                }
                Preconditions.checkArgument(area != 3, "AssetChangeRuleParse.parse temp[1] field is not found");
            } else {
                area = 0; // 全部
                opFieldNume = temp[1];
            }
            Long money = 0L;
            // 添加金钱操作
            switch (area) {
                case 0:
                    money = principal + interest;
                    break;
                case 1:
                    money = principal;
                    break;
                case 2:
                    money = interest;
                    break;
                default:
                    Preconditions.checkArgument(area != 4, "AssetChangeRuleParse.parse money exception");
            }

            // 反射操作
            Object property = PropertyUtils.getProperty(target, opFieldNume);
            if (property instanceof Long) {
                long value = (long) property;
                long result = 0;
                switch (op) {
                    case "add":
                        result = value + money;
                        break;
                    case "sub":
                        result = value - money;
                        break;
                    default:
                        throw new Exception(String.format("资金变动: %s-%s 操作符号不存在", opFieldNume, op));
                }
                Preconditions.checkArgument(result >= 0, "AssetsChangeRuleParse.parse money < 0");
                PropertyUtils.setProperty(target, opFieldNume, result);
            } else if (property instanceof Integer) {
                int value = (int) property;
                int result = 0;
                switch (op) {
                    case "add":
                        result = value + money.intValue();
                        break;
                    case "sub":
                        result = value - money.intValue();
                        break;
                    default:
                        throw new Exception(String.format("资金变动: %s-%s 操作符号不存在", opFieldNume, op));
                }
                Preconditions.checkArgument(money >= 0, "AssetsChangeRuleParse.parse money < 0");
                PropertyUtils.setProperty(target, opFieldNume, result);
            } else {
                throw new Exception(String.format("资金变动解析异常: %s 的类型不是Long或者Integer", opFieldNume));
            }
        }
    }
}
