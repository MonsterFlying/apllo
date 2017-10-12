package com.gofobao.framework.starfire.tender.vo.response;

import com.gofobao.framework.starfire.common.response.BaseResponse;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by master on 2017/10/11.
 */
@Data
public class BidRepaymentInfoRes extends BaseResponse {

    private Integer totalCount;

    List<Records> records = Lists.newArrayList();

    @Data
    public class Records {

        private String bid_id;

        private Integer repayCounts;

        List<RepayRecords> repayRecords = Lists.newArrayList();
    }

    @Data
    public class RepayRecords {

        private Integer repayPeriods;

        private Integer currentRepayPeriod;

        private String repayDate;

        private String actualRepayTime="";

        private String currentRepayCapital;

        private String currentRepayInterest;

        private String leftRepayCapital;

        private String leftRepayInterest;

        private String accruedRepayCapital;

        private String accruedRepayInterest;

        private Integer repayResult;

        private String repayType;


    }


}
