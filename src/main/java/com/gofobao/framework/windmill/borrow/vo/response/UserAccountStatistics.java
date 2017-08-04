package com.gofobao.framework.windmill.borrow.vo.response;

import lombok.Data;

/**
 * Created by admin on 2017/8/3.
 */

@Data
public class UserAccountStatistics {

    private Long retcode;

    private String retmsg;

    private String pf_user_id;

    private String all_balance;

    private String available_balance;

    private String frozen_money;

    private String reward;

    private String investing_interest;

    private String earned_interest;

    private String   current_money;
}
