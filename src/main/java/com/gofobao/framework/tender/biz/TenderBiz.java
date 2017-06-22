package com.gofobao.framework.tender.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.request.TenderUserReq;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.response.VoBorrowTenderUserWarpListRes;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Created by Zeke on 2017/5/31.
 */
public interface TenderBiz {
    /**
     * 创建投标
     * @param voCreateTenderReq
     * @return
     * @throws Exception
     */
    Map<String,Object> createTender(VoCreateTenderReq voCreateTenderReq) throws Exception;

    /**
     * 投标
     * @param voCreateTenderReq
     * @return
     */
    ResponseEntity<VoBaseResp> tender(VoCreateTenderReq voCreateTenderReq);

    /**
     * 投标用户
     * @param tenderUserReq
     * @return
     */
    ResponseEntity<VoBorrowTenderUserWarpListRes> findBorrowTenderUser(TenderUserReq tenderUserReq);


}
