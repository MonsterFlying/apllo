package com.gofobao.framework.finance.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.MyFinanceInvestBiz;
import com.gofobao.framework.finance.service.FinanceInvestService;
import com.gofobao.framework.finance.vo.response.VoViewFinanceTenderDetailWarpRes;
import com.gofobao.framework.system.vo.request.VoFinanceDetailReq;
import com.gofobao.framework.system.vo.response.VoViewFinanceReturnMoneyWarpRes;
import com.gofobao.framework.system.vo.response.VoViewFinanceReturnedMoney;
import com.gofobao.framework.system.vo.response.VoViewFinanceTenderDetail;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoFinanceInvestListReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/6.
 */
@Slf4j
@Service
public class MyFinanceInvestBizImpl implements MyFinanceInvestBiz {

    @Autowired
    private FinanceInvestService financeInvestService;


    @Override
    public ResponseEntity<VoViewBackMoneyListWarpRes> backMoneyList(VoFinanceInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.BACK_MONEY);
        try {
            Map<String, Object> resultMaps = financeInvestService.backMoneyList(voInvestListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoViewBackMoney> backMoneyList = (List<VoViewBackMoney>) resultMaps.get("backMoneyList");
            VoViewBackMoneyListWarpRes voViewBackMoneyListWarpRes = VoBaseResp.ok("查询成功", VoViewBackMoneyListWarpRes.class);
            voViewBackMoneyListWarpRes.setVoViewBackMonies(backMoneyList);
            voViewBackMoneyListWarpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewBackMoneyListWarpRes);
        } catch (Throwable e) {

            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewBackMoneyListWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewBiddingListWrapRes> biddingList(VoFinanceInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.BIDDING);
        try {
            Map<String, Object> resultMaps = financeInvestService.biddingList(voInvestListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoViewBiddingRes> biddingRes = (List<VoViewBiddingRes>) resultMaps.get("biddingResList");
            VoViewBiddingListWrapRes wrapResRes = VoBaseResp.ok("查询成功", VoViewBiddingListWrapRes.class);
            wrapResRes.setVoViewBiddingRes(biddingRes);
            wrapResRes.setTotalCount(totalCount);
            return ResponseEntity.ok(wrapResRes);
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewBiddingListWrapRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewSettleWarpRes> settleList(VoFinanceInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.SETTLE);
        try {
            Map<String, Object> resultMaps = financeInvestService.settleList(voInvestListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoViewSettleRes> settleList = (List<VoViewSettleRes>) resultMaps.get("settleResList");

            VoViewSettleWarpRes viewSettleWarpRes = VoBaseResp.ok("查询成功", VoViewSettleWarpRes.class);
            viewSettleWarpRes.setTotalCount(totalCount);
            viewSettleWarpRes.setVoViewSettleRes(settleList);
            return ResponseEntity.ok(viewSettleWarpRes);
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewSettleWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewFinanceTenderDetailWarpRes> tenderDetail(VoFinanceDetailReq voDetailReq) {
        try {
            VoViewFinanceTenderDetail viewTenderDetail = financeInvestService.tenderDetail(voDetailReq);
            VoViewFinanceTenderDetailWarpRes voViewTenderDetailWarpRes = VoBaseResp.ok("查询成功", VoViewFinanceTenderDetailWarpRes.class);
            voViewTenderDetailWarpRes.setVoViewTenderDetail(viewTenderDetail);
            return ResponseEntity.ok(voViewTenderDetailWarpRes);
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewFinanceTenderDetailWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewFinanceReturnMoneyWarpRes> infoList(VoFinanceDetailReq voDetailReq) {
        try {
            VoViewFinanceReturnedMoney voViewReturnedMoney = financeInvestService.infoList(voDetailReq);
            VoViewFinanceReturnMoneyWarpRes voViewReturnMoneyWarpRes = VoBaseResp.ok("", VoViewFinanceReturnMoneyWarpRes.class);
            voViewReturnMoneyWarpRes.setVoViewReturnedMoney(voViewReturnedMoney);
            return ResponseEntity.ok(voViewReturnMoneyWarpRes);
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewFinanceReturnMoneyWarpRes.class));
        }
    }
}
