package com.gofobao.framework.windmill.borrow.vo.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/4.
 */
@Data
public class InvestRecordsRes {
    private String pf_user_id;

    private Long retcode;

    private String retmsg;

    List<InvestRecords> invest_records = Lists.newArrayList();

}
