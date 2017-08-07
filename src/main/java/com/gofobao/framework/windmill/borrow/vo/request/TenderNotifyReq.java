package com.gofobao.framework.windmill.borrow.vo.request;

import lombok.Data;

/**
 * Created by admin on 2017/8/4.
 */

@Data
public class TenderNotifyReq {

    private String pf_user_id;

    private String invest_time;

    private String invest_sno;

    private String invest_money;

    private String invest_limit;

    private String invest_rate;

    private String back_way;

    private String invest_title;


}
