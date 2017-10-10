package com.gofobao.framework.starfire.tender.vo.response;

import com.gofobao.framework.starfire.common.response.BaseResponse;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by master on 2017/9/28.
 */
@Data
public class UserTenderRes extends BaseResponse {

    private Integer totalCount = 0;

    private List<UserRecords> records = Lists.newArrayList();


    @Data
    public class UserRecords {

        private String platform_uid;

        private String mobile;

        private String bidtotalCount;

        private List<UserbidRecords> userbidrecords = Lists.newArrayList();

    }

    @Data
    public class UserbidRecords {

        private String bid_id;

        private String rate;

        private String raiseRate;

        private String bidResult;

        private String productBidId;

        private String expireDate;

        private String interestDate="";

        private String investAmount;

        private String investTime;

        private String canAssign;

        private String profitAmount;

        private String isFirstInvest="false";

    }


}
