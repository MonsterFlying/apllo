package com.gofobao.framework.windmill.borrow.vo.response;

import lombok.Data;

/**
 * Created by admin on 2017/8/3.
 */
@Data
public class ByDayStatistics {

    private Long retcode;

    private String retmsg;

    private Long lend_count=0L;

    private Long borrow_count=0L;

    private String invest_all_money;

    private String all_wait_back_money;

}
