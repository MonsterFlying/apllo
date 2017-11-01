package com.gofobao.framework.wheel.borrow.vo.request;

import lombok.Data;

/**
 * Created by master on 2017/10/30.
 */
@Data
public class InvestNoticeReq {

    private String pf_user_id;

    private String all_balance;

    private String available_balance;

    private String frozen_money;

    private String reward;

    private String investing_principal;

    private String investing_interest;

    private String earned_interest;

    private String current_money;

    private String invest_time;

    private String invest_money;

    private String all_back_principal;

    private String all_back_interest;

    private String invest_reward;

    private String invest_record_id;

    private String project_id;

    private String project_title;

    private String project_url;

    private String project_rate;

    private String project_progress;

    private Integer project_timelimit;

    private String project_timelimit_desc;

    private String invest_status;

    private String interest_time;

    private Integer monthly_back_date;

    private String next_back_date = "";

    private String next_back_money = "";

    private String next_back_principal = "";

    private String next_back_interest = "";

    private String payback_way;

    private Integer attorn_state = 0;

    private String attorn_time = "";
}
