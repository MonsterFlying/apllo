package com.gofobao.framework.tender.service;

import com.gofobao.framework.tender.vo.request.VoTransferReq;

import java.util.Map;


/**
 * Created by admin on 2017/6/12.
 */

public interface TransferService {

    Map<String, Object> transferOfList(VoTransferReq voTransferReq);


    Map<String, Object> transferedList(VoTransferReq voTransferReq);

    Map<String, Object> transferMayList(VoTransferReq voTransferReq);

    Map<String,Object> transferBuyList(VoTransferReq voTransferReq);

}
