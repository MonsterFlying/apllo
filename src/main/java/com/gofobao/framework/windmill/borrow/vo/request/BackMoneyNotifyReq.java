package com.gofobao.framework.windmill.borrow.vo.request;

import lombok.Data;

/**
 * Created by admin on 2017/8/4.
 */
@Data
public class BackMoneyNotifyReq {

    private String pf_user_id;

    private String back_time;

    private Long bid_id;

    private String invest_sno;

    private String back_money;

    private String invest_title;

}
