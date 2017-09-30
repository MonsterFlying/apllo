package com.gofobao.framework.system.vo.response;

import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/2.
 */

@Data
public class VoViewFinanceReturnedMoney {

    private Integer orderCount;

    private String collectionMoneySum;

    private  List<FinanceReturnedMoney> returnedMonies;
}
