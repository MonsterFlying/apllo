package com.gofobao.framework.starfire.user.vo.response;

import com.gofobao.framework.starfire.common.response.BaseResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by master on 2017/9/29.
 */
@Data
public class UserAccountRes extends BaseResponse {

    private Integer totalCount = 0;

    private List<Records> records = new ArrayList<>(0);

    @Data
    public class Records {

        private Long platform_uid;

        private String mobile;

        private List<AccountRecords> accountRecords = new ArrayList<>(0);

    }

    @Data
    public class AccountRecords {

        private String assetsAmount;

        private String balanceAmount;

        private String frozenCapital;

        private String availableBanlance;

        private String bidAmount="0.00";

        private String profitAmount;

        private String todayProfitAmount="0.00";

        private String uncollectedInterest;

        private String date;


    }

}
