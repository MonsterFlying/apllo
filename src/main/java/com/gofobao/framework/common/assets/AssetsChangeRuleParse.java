package com.gofobao.framework.common.assets;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 资金变动规则解析
 * Created by Administrator on 2017/7/10 0010.
 */
@Slf4j
public class AssetsChangeRuleParse {

    /**
     *  解析规则
     * @param target
     * @param rule
     * @param principal
     * @param interest
     * @param fee
     * @throws Exception
     */
    public static void parse(Object target, String rule, long principal, long interest, long fee) throws Exception{
        //==================================
        // 数据检查
        //==================================
        Preconditions.checkNotNull(target,
                "AssetsChangeRuleParse.parse target is null ");
        Preconditions.checkNotNull(rule,
                "AssetsChangeRuleParse.parse rule is null ");
        Preconditions.checkArgument(principal >= 0 ,
                "AssetsChangeRuleParse.parse principal < 0 ");
        Preconditions.checkArgument(interest >= 0 ,
                "AssetsChangeRuleParse.parse interest < 0 ");
        Preconditions.checkArgument(fee >= 0 ,
                "AssetsChangeRuleParse.parse fee < 0 ");

        //==================================
        // 规则解析
        //==================================
        String[] ruleArr = rule.split(",");
        String op =  null, opFieldNume = null;
        String [] temp = null ;
        long money = 0 ;
        int area = 0;
        for(String subRule: ruleArr){
            Preconditions.checkNotNull(!StringUtils.isEmpty(subRule),
                    "AssetsChangeRuleParse.parse subRule is empty") ;
            temp = subRule.split("@");
            Preconditions.checkArgument(temp.length == 2,
                    "AssetsChangeRuleParse.parse temp length not equals 2");

            op = temp[0] ;
            if(temp[1].contains("#")){
                temp = temp[1].split("#") ;
                Preconditions.checkArgument(temp.length == 2,
                        "AssetsChangeRuleParse.parse temp[1] length not equals 2");
                opFieldNume = temp[0] ;
                if( "principal".equals(temp[1])){
                    area = 1 ;
                }else if("interest".equals(temp[1])){
                    area = 2 ;
                }else if("fee".equals(temp[1])){
                    area = 3 ;
                }else{
                    area = 4 ;
                }
                Preconditions.checkArgument(area != 4,
                        "AssetsChangeRuleParse.parse temp[1] field is not found");
            }else{
                area = 0 ; // 全部
                opFieldNume = temp[1] ;
            }

            // 添加金钱操作
            switch (area){
                case  0:
                    money = principal + interest + fee ;
                    break;
                case 1:
                    money = principal;
                    break;
                case 2:
                    money = interest ;
                    break;
                case 3:
                    money = fee ;
                    break;
                default:
                        throw new Exception("AssetsChangeRuleParse.parse op exception") ;
            }

            // 反射操作
            Object property = PropertyUtils.getProperty(target, opFieldNume);
            if( !(property instanceof Long) ){
                Preconditions.checkArgument(area != 4,
                        "AssetsChangeRuleParse.parse field type is null long");
            }
            long value = (Long)property;
            switch (op){
                case "add":
                    money = value + money ;
                    break;
                case "sub":
                    money = value - money ;
                    break;
                default:
                    Preconditions.checkArgument(area != 4,
                            "AssetsChangeRuleParse.parse oparetor exception");
            }

            Preconditions.checkArgument(money >= 0, "AssetsChangeRuleParse.parse money < 0");
            PropertyUtils.setProperty(target, opFieldNume, money);
        }
    }
}
