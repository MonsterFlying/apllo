package com.gofobao.framework.borrow.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/15.
 */
@Data
public class VoViewBorrowStatisticsWarpRes extends VoBaseResp{
    private List<BorrowStatistics>  statisticsList= Lists.newArrayList();

}
