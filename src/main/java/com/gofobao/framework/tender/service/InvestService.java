package com.gofobao.framework.tender.service;

import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;

import java.util.List;

/**
 * Created by admin on 2017/6/1.
 */
public interface InvestService {

    /**
     * 回款中列表
     * @param voInvestListReq
     * @return
     */
    List<VoViewBackMoney> backMoneyList(VoInvestListReq voInvestListReq);

    /**
     * 投标中列表
     * @param voInvestListReq
     * @return
     */
    List<VoViewBiddingRes>biddingList(VoInvestListReq voInvestListReq);

    /**
     * 已结清
     * @param voInvestListReq
     * @return
     */
    List<VoViewSettleRes> settleList(VoInvestListReq voInvestListReq);


    /**
     * 已结清 and 回款中 详情
     * @param voDetailReq
     * @return
     */
    VoViewTenderDetail  tenderDetail(VoDetailReq voDetailReq);

    /**
     * 回款详情
     * @param voDetailReq
     * @return
     */
    VoViewReturnedMoney infoList(VoDetailReq voDetailReq);

}
