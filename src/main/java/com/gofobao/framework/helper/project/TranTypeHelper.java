package com.gofobao.framework.helper.project;


import com.qiniu.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TranTypeHelper {

    static Map<String, String> DATA = new HashMap<>(0);

    static {
        DATA.put("2616", "银联代收付渠道资金转出");
        DATA.put("2780", "P2P融资扣款");
        DATA.put("2781", "P2P到期还款");
        DATA.put("2788", "P2P代偿还款");
        DATA.put("2789", "P2P债权转让资金转出");
        DATA.put("2792", "P2P红包发放收益扣款");
        DATA.put("2793", "P2P平台贴息收益扣款");
        DATA.put("2820", "行内渠道资金转出");
        DATA.put("2831", "P2P债权转让资金转出");
        DATA.put("2833", "红包转出");
        DATA.put("4616", "转账手续费");
        DATA.put("4780", "P2P融资扣款手续费");
        DATA.put("4781", "P2P到期还款手续费");
        DATA.put("4788", "P2P代偿还款手续费");
        DATA.put("4820", "中间业务转出手续费");
        DATA.put("5500", "活期收益");
        DATA.put("5504", "靠档计息");
        DATA.put("7616", "银联代收付渠道资金转入");
        DATA.put("7722", "手续费入账");
        DATA.put("7724", "P2P提现手续费转入");
        DATA.put("7725", "P2P债权转让手续费转入");
        DATA.put("7777", "批量入账 ");
        DATA.put("7780", "P2P融资");
        DATA.put("7781", "P2P到期收益");
        DATA.put("7782", "P2P账户批量充值");
        DATA.put("7783", "P2P账户红包发放");
        DATA.put("7785", "P2P债权转让资金转入");
        DATA.put("7788", "P2P代偿还款到期收益");
        DATA.put("7792", "P2P红包发放收益");
        DATA.put("7820", "行内渠道资金转入");
        DATA.put("7822", "chinapay渠道资金转入");
        DATA.put("7831", "P2P债权转让资金转入");
        DATA.put("7833", "红包转入");
        DATA.put("7835", "债权转让手续费转入");
        DATA.put("7901", "直销银行账户资金转入");
        DATA.put("7905", "直销银行账户资金转入");
        DATA.put("7906", "直销银行账户资金转入");
        DATA.put("7907", "直销银行账户资金转入");
        DATA.put("7909", "直销银行账户资金转入");
        DATA.put("7910", "直销银行账户资金转入");
        DATA.put("9780", "P2P融资");
        DATA.put("9781", "P2P到期收益");
        DATA.put("9785", "批量债权转让手续费");
        DATA.put("9788", "P2P代偿收益");
        DATA.put("9831", "P2P债权转让资金转入");
    }

    public static String getMsg(String key) {
        String msg = DATA.get(key);
        if (StringUtils.isNullOrEmpty(msg)) {
            return "未知交易类型";
        }

        return msg;
    }
}
