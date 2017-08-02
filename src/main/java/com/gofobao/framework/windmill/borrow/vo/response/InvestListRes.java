package com.gofobao.framework.windmill.borrow.vo.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/1.
 */
@Data
public class InvestListRes {

    private Long retcode;

    private String retmsg;

    private List<Invest>invest_list= Lists.newArrayList();

}
