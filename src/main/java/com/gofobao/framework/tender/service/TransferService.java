package com.gofobao.framework.tender.service;

import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.response.TransferMay;
import com.gofobao.framework.tender.vo.response.TransferOf;
import com.gofobao.framework.tender.vo.response.Transfered;

import java.util.List;


/**
 * Created by admin on 2017/6/12.
 */

public interface TransferService {

    List<TransferOf> transferOfList(VoTransferReq voTransferReq);


    List<Transfered> transferedList(VoTransferReq voTransferReq);

    List<TransferMay> transferMayList(VoTransferReq voTransferReq);

}
