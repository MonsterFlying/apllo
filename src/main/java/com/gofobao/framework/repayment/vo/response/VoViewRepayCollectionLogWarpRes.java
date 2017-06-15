package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/15.
 */
@Data
public class VoViewRepayCollectionLogWarpRes extends VoBaseResp{

    private List<RepayCollectionLog> collectionLogs= Lists.newArrayList();
}
