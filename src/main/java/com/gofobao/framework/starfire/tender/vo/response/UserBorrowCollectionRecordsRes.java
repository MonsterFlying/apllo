package com.gofobao.framework.starfire.tender.vo.response;

import com.gofobao.framework.starfire.common.response.BaseResponse;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by master on 2017/9/28.
 */
@Data
public class UserBorrowCollectionRecordsRes extends BaseResponse {

    private Integer totalCount=0;

    private List<Records> records = Lists.newArrayList();

    @Data
    public class Records {

        private String platform_uid;

        private Integer bidCount;

        private List<BidRecords> bidRecords = Lists.newArrayList();
    }

    @Data
    public class BidRecords {
        private String bid_id;

        private String productBidId;

        private Integer bidRepayCount;

        private List<BidRepayRecords> bidRepayRecords = Lists.newArrayList();

    }
    @Data
    public class BidRepayRecords {

        private Integer repayPeriods;

        private Integer currentRepayPeriod;

        private String repayDate;

        private String actualRepayTime;

        private String currentRepayCapital;

        private String currentRepayInterest;

        private String leftRepayCapital;

        private String leftRepayInterest;

        private String accruedRepayCapital;

        private String accruedRepayInterest;

        private Integer repayResult;

        private String repayType="";

    }


}
