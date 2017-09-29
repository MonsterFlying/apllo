package com.gofobao.framework.starfire.tender.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gofobao.framework.starfire.common.response.BaseResponse;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by master on 2017/9/29.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BorrowQueryRes extends BaseResponse {

    private String totalCount;

    private List<Records> records = Lists.newArrayList();

    @Data
    public class Records {

        private Long bid_id;

        private String bid_name;

        private String bid_type;

        private String guarantee_type;

        private String borrow_amount;

        private String left_amount;

        private String borrower_area="";

        private String borrower_address="";

        private String bid_rate;

        private String raise_rate;

        private String interest_date;

        private String repay_type;

        private String repay_count;

        private String bid_status;

        private String bond_code;

        private String bid_url;

        private String wap_bid_url;

        private Boolean isPromotion;

        private Boolean isRecommend;

        private Boolean isNovice;

        private Boolean isExclusive;

        private Boolean isAssignment;

        private Boolean canAssign;

        private String bid_progress_percent;

        private String introduction;

        private String duration_months="";

        private String duration_days="";

        private Boolean isDurationMonths=true;
    }

}
