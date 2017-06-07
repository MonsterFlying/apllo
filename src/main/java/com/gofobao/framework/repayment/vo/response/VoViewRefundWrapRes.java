package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/5.
 */
@Data
public class VoViewRefundWrapRes extends VoBaseResp{
    private List<VoViewRefundRes> list =  Lists.newArrayList() ;
}
