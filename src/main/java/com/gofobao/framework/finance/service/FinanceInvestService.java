package com.gofobao.framework.finance.service;

import com.gofobao.framework.system.vo.request.VoFinanceDetailReq;
import com.gofobao.framework.system.vo.response.VoViewFinanceReturnedMoney;
import com.gofobao.framework.system.vo.response.VoViewFinanceTenderDetail;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoFinanceInvestListReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.VoViewReturnedMoney;
import com.gofobao.framework.tender.vo.response.VoViewTenderDetail;

import java.util.Map;

/**
 * Created by admin on 2017/6/1.
 */
public interface FinanceInvestService {

    /**
     * 回款中列表
     * @param voInvestListReq
     * @return
     */
    Map<String, Object> backMoneyList(VoFinanceInvestListReq voInvestListReq);

    /**
     * 投标中列表
     * @param voInvestListReq
     * @return
     */
    Map<String, Object> biddingList(VoFinanceInvestListReq voInvestListReq);

    /**
     * 已结清
     * @param voInvestListReq
     * @return
     */
    Map<String, Object> settleList(VoFinanceInvestListReq voInvestListReq);


    /**
     * 已结清 and 回款中 详情
     * @param voDetailReq
     * @return
     */
    VoViewFinanceTenderDetail tenderDetail(VoFinanceDetailReq voDetailReq);

    /**
     * 回款详情
     * @param voDetailReq
     * @return
     */
    VoViewFinanceReturnedMoney infoList(VoFinanceDetailReq voDetailReq) ;

}
