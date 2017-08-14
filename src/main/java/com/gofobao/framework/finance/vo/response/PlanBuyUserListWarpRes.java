package com.gofobao.framework.finance.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/14.
 */
@Data
public class PlanBuyUserListWarpRes extends VoBaseResp {

    private List<PlanBuyer> tenderUsers= Lists.newArrayList();
}
