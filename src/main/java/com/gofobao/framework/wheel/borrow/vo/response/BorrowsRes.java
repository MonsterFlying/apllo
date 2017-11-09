package com.gofobao.framework.wheel.borrow.vo.response;

import com.gofobao.framework.wheel.common.BaseResponse;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @author master
 * @date 2017/10/27
 */
@Data
public class BorrowsRes extends BaseResponse {

    private List<BorrowInfo>invest_list= Collections.emptyList();

    @Data
    public class BorrowInfo {

        private Long invest_id;

        private String invest_time;

        private String buy_unit;

        private String buy_limit;

        private String invest_url;

        private Integer time_limit;

        private String time_limit_desc;

        private String total_amount;

        private String rate;

        private String progress;

        private String start_time;

        private String payback_way;

        private String invest_condition;

        private String project_description;

        private Integer lose_invest=0;
    }

}
