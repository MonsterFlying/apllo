package com.gofobao.framework.windmill.borrow.vo.request;

import lombok.Data;

/**
 * Created by admin on 2017/8/4.
 */
@Data
public class UserTenderLogReq {

    private String pf_user_id;

    private String start_time;

    private String end_time;

    private Integer invest_status;

    private Integer offset;

    private Integer limit;

    private Long invest_record_id;

}
