package com.gofobao.framework.finance.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.vo.response.VoViewFinanceTenderDetail;
import com.gofobao.framework.tender.vo.response.VoViewTenderDetail;
import lombok.Data;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewFinanceTenderDetailWarpRes extends VoBaseResp {

    private VoViewFinanceTenderDetail voViewTenderDetail=new VoViewFinanceTenderDetail();

}
