package com.gofobao.framework.repayment.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/8.
 */
@Data
public class VoViewLoanStatisticsWarpRes extends VoBaseResp {

    private Integer totalCount=0;

    private List<LoanStatistics> statisticss= Lists.newArrayList();

}
