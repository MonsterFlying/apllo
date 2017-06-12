package com.gofobao.framework.tender.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/12.
 */
@Slf4j
@Service
public class TransferBizImpl implements TransferBiz {

    @Autowired
    private TransferService transferService;

    @Override
    public ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(VoTransferReq voTransferReq) {
        try {
            List<TransferOf> transferOfs= transferService.transferOfList(voTransferReq);
            VoViewTransferOfWarpRes voViewTransferOfWarpRes= VoBaseResp.ok("查询成功",VoViewTransferOfWarpRes.class);
            voViewTransferOfWarpRes.setTransferOfs(transferOfs);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        }catch (Exception e){
            log.info("TransferBizImpl tranferOfList query fail%S",e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferOfWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewTransferedWarpRes> transferedlist(VoTransferReq voTransferReq) {
        try {
            List<Transfered> transfereds= transferService.transferedList(voTransferReq);
            VoViewTransferedWarpRes voViewTransferOfWarpRes= VoBaseResp.ok("查询成功",VoViewTransferedWarpRes.class);
            voViewTransferOfWarpRes.setTransferedList(transfereds);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        }catch (Exception e){
            log.info("TransferBizImpl transferedlist query fail%S",e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferedWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewTransferMayWarpRes> transferMayList(VoTransferReq voTransferReq) {
        try {
            List<TransferMay> transferOfs= transferService.transferMayList(voTransferReq);
            VoViewTransferMayWarpRes voViewTransferOfWarpRes= VoBaseResp.ok("查询成功",VoViewTransferMayWarpRes.class);
            voViewTransferOfWarpRes.setMayList(transferOfs);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        }catch (Exception e){
            log.info("TransferBizImpl transferMayList query fail%S",e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferMayWarpRes.class));
        }
    }
}
