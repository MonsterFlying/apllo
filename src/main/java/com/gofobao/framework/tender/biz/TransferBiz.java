package com.gofobao.framework.tender.biz;

import com.gofobao.framework.tender.vo.request.VoTransferReq;
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

}
