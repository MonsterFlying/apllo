package com.gofobao.framework.windmill.borrow.vo.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/3.
 */
@Data
public class BySomeDayRes {

    private Long retcode;

    private String retmsg;

    private List<BySomeDay> invest_list= Lists.newArrayList();
}
