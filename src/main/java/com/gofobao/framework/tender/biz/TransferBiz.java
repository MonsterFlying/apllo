package com.gofobao.framework.tender.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.request.VoGoTenderReq;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.request.VoTransferTenderReq;
import com.gofobao.framework.tender.vo.response.VoGoTenderInfo;
import com.gofobao.framework.tender.vo.response.VoViewTransferMayWarpRes;
import com.gofobao.framework.tender.vo.response.VoViewTransferOfWarpRes;
import com.gofobao.framework.tender.vo.response.VoViewTransferedWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/12.
 */
public interface TransferBiz {


    ResponseEntity<VoViewTransferOfWarpRes>tranferOfList(VoTransferReq voTransferReq);


    ResponseEntity<VoViewTransferedWarpRes>transferedlist(VoTransferReq voTransferReq);


    ResponseEntity<VoViewTransferMayWarpRes>transferMayList(VoTransferReq voTransferReq);

    /**
     * 债权转让
     * @param voTransferTenderReq
     * @return
     */
    ResponseEntity<VoBaseResp> transferTender(VoTransferTenderReq voTransferTenderReq);

    /**
     * 获取立即转让详情
     *
     * @param voGoTenderReq 投标记录Id
     * @return
     */
    ResponseEntity<VoGoTenderInfo> goTenderInfo(VoGoTenderReq voGoTenderReq);
}
