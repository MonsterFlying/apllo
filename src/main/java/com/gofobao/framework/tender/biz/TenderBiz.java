package com.gofobao.framework.tender.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/31.
 */
public interface TenderBiz {
    ResponseEntity<VoBaseResp> createTender(VoCreateTenderReq voCreateTenderReq);
}
