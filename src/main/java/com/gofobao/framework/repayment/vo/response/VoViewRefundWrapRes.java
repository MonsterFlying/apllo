package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/5.
 */
@Data
public class VoViewRefundWrapRes extends VoBaseResp{
    List<VoViewRefundRes> voViewRefundRes = Collections.EMPTY_LIST ;
}
