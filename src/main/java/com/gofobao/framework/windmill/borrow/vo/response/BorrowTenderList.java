package com.gofobao.framework.windmill.borrow.vo.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/2.
 */
@Data
public class BorrowTenderList {

    private Long retcode;

    private String retmsg;

    private String first_invest_time;

    private String last_invest_time;

    private Integer all_investors;

    private List<VoTender>invest_list= Lists.newArrayList();

}
