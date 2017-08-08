package com.gofobao.framework.windmill.borrow.vo.response;

import lombok.Data;

/**
 * Created by admin on 2017/8/4.
 */
@Data
public class InvestRecords {

    private String invest_time;

    private String invest_money;

    private String all_back_principal="0";

    private String all_back_interest="0";

    private String all_interest;

    private Long invest_record_id;

    private String project_url;

    private String invest_reward;

    private String project_title;

    private Long project_id;

    private String project_rate;

    private Integer project_timelimit;

    private String project_timelimit_desc;

    private Integer invest_status;

    private Integer is_auto_bid0;

    private Integer monthly_back_date;

    private String next_back_date;

    private String next_back_money;

    private String next_back_principal;

    private String next_back_interest;

    private String payback_way;

    private Integer attorn_state;

    private String attorn_time;


}
