package com.gofobao.framework.tender.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/6/1.
 */
public interface TenderThirdBiz {
    ResponseEntity<VoBaseResp> createThirdTender(VoCreateThirdTenderReq voCreateThirdTenderReq);
}
