package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewRepaymentDetailWrapRes extends VoBaseResp {
    private  VoViewRepaymentDetail viewRepaymentDetail=new VoViewRepaymentDetail();
}
