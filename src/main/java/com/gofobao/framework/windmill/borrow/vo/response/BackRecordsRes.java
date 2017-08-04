package com.gofobao.framework.windmill.borrow.vo.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/4.
 */
@Data
public class BackRecordsRes {

    private String retmsg;

    private Long retcode;

    private String invest_record_id;

    private List<BackRecords> back_records = Lists.newArrayList();
}
