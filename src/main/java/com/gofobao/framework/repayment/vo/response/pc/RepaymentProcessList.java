package com.gofobao.framework.repayment.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;

import java.util.List;

/**
 * Created by master on 2017/9/23.
 */


@ApiModel
public class RepaymentProcessList extends VoBaseResp {
    List<RepaymentProcess>repaymentProcesses= Lists.newArrayList();

}
