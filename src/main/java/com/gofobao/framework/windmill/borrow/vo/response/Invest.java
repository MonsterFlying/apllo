package com.gofobao.framework.windmill.borrow.vo.response;

import lombok.Data;

/**
 * Created by admin on 2017/8/1.
 */
@Data
public class Invest {

    private Long invest_id;

    private String invest_title;

    private String invest_url;

    private Integer time_limit;

    private String time_limit_desc;

    private String buy_limit;

    private String buy_unit;

    private String invested_amount;

    private String total_amount;

    private String rate;

    private String progress;

    private String start_time;

    private String payback_way;

    private String invest_condition;

    private String project_description;

    private Integer lose_invest;
}
