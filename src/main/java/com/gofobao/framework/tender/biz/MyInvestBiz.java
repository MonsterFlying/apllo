package com.gofobao.framework.tender.biz;

import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/6.
 */
public interface MyInvestBiz {


    /**
     * 回款中列表
     * @param voInvestListReq
     * @return
     */
    ResponseEntity<VoViewBackMoneyListWarpRes> backMoneyList(VoInvestListReq voInvestListReq);

    /**
     * 投标中列表
     * @param voInvestListReq
     * @return
     */
    ResponseEntity<VoViewBiddingListWrapRes> biddingList(VoInvestListReq voInvestListReq);

    /**
     * 已结清
     * @param voInvestListReq
     * @return
     */
    ResponseEntity<VoViewSettleWarpRes> settleList(VoInvestListReq voInvestListReq);


    /**
     * 已结清 and 回款中 详情
     * @param voDetailReq
     * @return
     */
    ResponseEntity<VoViewTenderDetailWarpRes>  tenderDetail(VoDetailReq voDetailReq);

    /**
     * 回款详情
     * @param voDetailReq
     * @return
     */
    ResponseEntity<VoViewReturnMoneyWarpRes> infoList(VoDetailReq voDetailReq);

}
